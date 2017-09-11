(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('GCLogAnalyzerController',
            function($element, $compile, $scope, $timeout, analyzerService, $location,gclogDurationRange,toastr) {

                var vm = this;
                var formatInteger = d3.format(',.0f');
                analyzerService.getJVMOptions(function(data) {

                    vm.jvmOptions = data;
                });
                vm.stats = {
                    totalCount: 0,
                    totalReclaimedBytes: 0,
                    totalTime: 0,
                    avgTime: 0,
                    avgTimeStdDev: 0,
                    minTime: 0,
                    maxTime: 0,
                    intervalAvgTime: 0,
                    totalPromotedBytes: 0,
                    reclaimedBytesRate: '',
                    promotedBytesRate: ''
                };
                vm.GCLogs = [];
                var gcDatas = [];
                vm.heapSize = [];
                vm.jvmHeapData = [];
                vm.interactiveGraphData = [];
                vm.reclaBytesData = [{
                    values: [
                        ['Minor GC',
                            0
                        ],
                        [
                            'Full GC',
                            0
                        ]
                    ]
                }];
                vm.cumuTimeData = [
                    ['Minor GC', 0],
                    ['Full GC', 0]
                ];
                vm.avgTimeData = [{
                    values: [
                        ['Minor GC',
                            0
                        ],
                        [
                            'Full GC',
                            0
                        ]
                    ]
                }];
                analyzerService.getGCLogList(function(data) {

                    vm.GCLogs = data;
                    var name = $location.search().name;

                    if (name) {
                        vm.seleGCLog = name;
                    } else {
                        vm.seleGCLog = vm.GCLogs[0];
                    }
                    analyzerService.parseGCLog(vm.seleGCLog, function(raw) {
                        parseGCLog(JSON.parse(raw));
                    });

                });


                vm.logFileChange = function() {
                    analyzerService.parseGCLog(vm.seleGCLog, function(raw) {
                        $location.search({
                            'name': vm.seleGCLog
                        });
                        parseGCLog(JSON.parse(raw));
                    });
                };

                vm.kpi = {
                    throughput: 99.99,
                    avgTime: 0,
                    maxTime: 0,
                    durations: []
                };

                vm.kpiData = [];


                function chooseOne(a, b, needMax) {

                    if (a && b) {
                        return a > b ? (needMax ? a : b) : (needMax ? b : a);

                    } else if (a) {
                        return a;
                    } else if (b) {
                        return b;
                    }
                }

                function switchToHeapAfterGC(data) {
                        vm.interactiveGraphOptions.chart.type = 'lineChart';
                        vm.interactiveGraphOptions.chart.showLegend = false;
                        vm.interactiveGraphOptions.chart.forceY = [0, 5];
                        vm.interactiveGraphOptions.title.text = 'Heap after GC';
                        vm.interactiveGraphOptions.chart.yAxis.axisLabel = 'Heap Size(mb)';
                        vm.interactiveGraphOptions.chart.callback = function(chart) {
                            d3.select(chart.container).selectAll('path.nv-point').attr('class', function(d) {

                                var oldClass = d3.select(this).attr('class') || '';
                                if (d[0][1]) {
                                    //d3.select(this).style('stroke-width', '10').style('stroke', '#89ff98');
                                    return oldClass + ' fullgc-mark';
                                } else {
                                    return oldClass;
                                }
                            });
                        };
                        vm.interactiveGraphOptions.chart.x = function(x) {
                            return x ? x[0] || 0 : 0;
                        };
                        vm.interactiveGraphOptions.chart.y = function(x) {
                            return x ? x[7] : 0;
                        };
                        vm.interactiveGraphData = [{
                            "key": 'after GC',
                            'area': true,
                            'values': data
                        }];

                    }
                    //0,gc time
                    //1,isFullGC
                    //2,duration
                    //3,minorArea beforeGC
                    //4,minorArea afterGC
                    //5,youngGen
                    //6,heap beforeGC
                    //7,heap afterGC
                    //8, heap
                    //9,old beforeGC
                    //10,old afterGC
                    //11,oldGen

                var isSeleFirst = false;

                function parseGCLog(rawData) {
                    vm.interactiveGraphOptions = {
                        chart: {
                            type: 'lineChart',
                            color: colors,
                            height: 450,
                            margin: {
                                top: 20,
                                right: 20,
                                bottom: 40,
                                left: 60
                            },
                            x: function(d) {
                                if (d instanceof Array)
                                    return d[0];
                                else return d;
                            },
                            y: function(d) {
                                if (d instanceof Array)
                                    return d[1];
                                else return d;
                            },
                            shape: function(d) {
                                return 'square';
                            },
                            clipEdge: true,
                            duration: 100,
                            useInteractiveGuideline: true,
                            xAxis: {
                                showMaxMin: false,
                                axisLabel: 'Time UTC+0800',
                                tickFormat: function(d) {
                                    return d3.time.format('%X %b %d')(new Date(d));
                                }
                            },
                            yAxis: {
                                axisLabel: 'Heap Size(mb)',
                                tickFormat: function(d) {
                                    return d3.format(',.0f')(d / 1024);
                                }
                            },
                            zoom: {
                                enabled: true,
                                scaleExtent: [1, 50],
                                useFixedDomain: false,
                                useNiceScale: false,
                                horizontalOff: false,
                                verticalOff: false,
                                unzoomEventType: 'dblclick.zoom'
                            }
                        },

                        title: {
                            enable: true,
                            text: 'Heap Usage'
                        },
                    };
                    vm.operations = [{
                        name: 'Heap after GC',
                        selected: true
                    }, {
                        name: 'Heap before GC'
                    }, {
                        name: 'GC Duration'
                    }, {
                        name: 'Reclaimed Bytes'
                    }, {
                        name: 'Young Gen'
                    }, {
                        name: 'Old Gen'
                    }, {
                        name: 'A & P'
                    }];

                    vm.seleOpr = vm.operations[0];
                    var minorTotalReclaimed = 0;
                    var fullTotalReclaimed = 0;
                    var totalMinorTime = 0;
                    var totalFullTime = 0;
                    var minorCount = 0;
                    var fullCount = 0;
                    var minorMinTime;
                    var minorMaxTime;
                    var fullMinTime;
                    var fullMaxTime;
                    vm.stats.totalPromotedBytes = 0;
                    vm.stats.intervalAvgTime = '';

                    vm.stats.minorIntervalAvg = '';
                    vm.stats.fullIntervalAvg = '';

                    var data = rawData;
                    var youngGen = 0,
                        youngPeak = 0;
                    var oldGen = 0,
                        oldPeak = 0;
                    var permGen = 0,
                        permPeak = 0;
                    var totalGCTime = 0;
                    var maxGCTime = 0;
                    var totalDateDistance = 0,
                        minorDateDistance = 0,
                        fullDateDistance = 0;
                    var lastMinorDate, lastFullDate;
                    var m = 0,
                        d, s = 0,
                        j = 0;
                    var mm = 0,
                        md, ms = 0,
                        mj = 0;
                    var fm = 0,
                        fd, fs = 0,
                        fj = 0;
                    var fullTotalAfterGC = 0;
		    var MaxDuration = 100000000;

                    var durationRange = [0.1,MaxDuration];

		    if(gclogDurationRange &&  gclogDurationRange.length){

			    var tmp = [];
			    var hasError = false;
			     angular.forEach(gclogDurationRange.split(','),function(x){
				     if(isNaN(x)){
					     hasError = true;
					     return false;
				     }
				     tmp.push(x-0);
			     });
			     if(!hasError){
				     durationRange = tmp;
				     durationRange.push(MaxDuration);
			     }else{
				     toastr.error('Wrong gclog.duration.range config!');
			     }
			    
		    }

                    var durationStats = {};
                    data.forEach(function(x, i) {
                        x[0] = new Date(x[0]);
                        var cost = x[2];

                        if (i > 0) {
                            totalDateDistance += (x[0] - data[i - 1][0]);

                        }
                        totalGCTime += cost;
                        if (cost > maxGCTime) {
                            maxGCTime = cost;
                        }


                        d = cost - m;
                        m += d / ++j;
                        s += d * (cost - m);
                        for (var j = 0; j < durationRange.length; j++) {
                            var preD = (j === 0 ? 0 : durationRange[j - 1]);
                            var curD = durationRange[j];
                            if (cost >= preD && cost < curD) {

                                durationStats[curD] = durationStats[curD] || {
                                    'count': 0,
                                    'name': preD + '-' + (curD==MaxDuration?'': curD),
                                    'percentage': 0
                                };
                                durationStats[curD].count++;
                            }

                        }
                        if (!x[1]) {
                            md = cost - mm;
                            mm += md / ++mj;
                            ms += md * (cost - mm);
                            minorCount++;
                            if (minorCount > 1) {
                                minorDateDistance += (x[0] - lastMinorDate);
                            }
                            totalMinorTime += cost;
                            minorTotalReclaimed += x[6] - x[7];
                            if (!minorMinTime) {
                                minorMinTime = cost;
                            } else if (minorMinTime > cost) {
                                minorMinTime = cost;
                            }
                            if (!minorMaxTime) {
                                minorMaxTime = cost;
                            } else if (minorMaxTime < cost) {
                                minorMaxTime = cost;
                            }
                            lastMinorDate = x[0];
                        } else {
                            fullCount++;
                            totalFullTime += cost;
                            fd = cost - fm;
                            fm += fd / ++fj;
                            fs += fd * (cost - fm);
                            if (fullCount > 1) {
                                fullDateDistance += (x[0] - lastFullDate);
                            }

                            if (!x[11]) {
                                x[6] = data[i - 1][6];
                                x[3] = data[i - 1][3];
                            } else {
                                permGen = x[11];
                                if (x[9] > permPeak) {
                                    permPeak = x[9];
                                }
                                x[9] = x[3];
                                x[10] = x[4];
                                x[11] = x[5];
                                x[3] = x[6] - x[9];
                                x[4] = x[7] - x[10];
                                x[5] = x[8] - x[11];
                            }
                            fullTotalReclaimed += x[6] - x[7];
                            if (!fullMinTime) {
                                fullMinTime = cost;
                            } else if (fullMinTime > cost) {
                                fullMinTime = cost;
                            }
                            if (!fullMaxTime) {
                                fullMaxTime = cost;
                            } else if (fullMaxTime < cost) {
                                fullMaxTime = cost;
                            }
                            lastFullDate = x[0];

                        }
                        if (!x[11]) {
                            x[9] = x[6] - x[3];
                            x[10] = x[7] - x[4];
                            x[11] = x[8] - x[5];
                        }
                        if (youngGen === 0) {
                            youngGen = x[5] * 1024;
                        }
                        if (x[11] > oldGen) {
                            oldGen = x[11];
                        }
                        var tmp = x[3] > x[4] ? x[3] : x[4];
                        if (tmp > youngPeak) {
                            youngPeak = tmp;
                        }
                        tmp = x[9] > x[10] ? x[9] : x[10];
                        if (tmp > oldPeak) {
                            oldPeak = tmp;
                        }

                        var promoBytes = x[10] - x[9];
                        vm.stats.totalPromotedBytes += (promoBytes > 0 ? promoBytes : 0);


                        fullTotalAfterGC += x[7];

                    });

                    oldGen *= 1024;
                    permGen *= 1024;
                    permPeak *= 1024;


                    vm.suggestHeapMin = (fullTotalAfterGC / data.length) * 1024 * 3;
                    vm.suggestHeapMax = vm.suggestHeapMin * 4 / 3;


                    vm.totalHeap = data[0][8] * 1024;
                    vm.youngHeap = data[0][5] * 1024;
                    vm.tenuredHeap = data[0][11] * 1024;
                    var totalCostTime = data[data.length - 1][0] - data[0][0];
                    vm.spendTime = VIUtil.calculateRunTime(totalCostTime);
                    vm.stats.totalReclaimedBytes = (minorTotalReclaimed + fullTotalReclaimed) * 1024;
                    vm.stats.totalPromotedBytes *= 1024;
                    vm.stats.reclaimedBytesRate = (vm.stats.totalReclaimedBytes * 1000 / totalCostTime);
                    vm.stats.promotedBytesRate = (vm.stats.totalPromotedBytes * 1000 / totalCostTime);
                    vm.stats.totalCount = minorCount + fullCount;
                    vm.stats.minorCount = minorCount;
                    vm.stats.fullCount = fullCount;
                    vm.stats.minorReclaimedBytes = minorTotalReclaimed * 1024;
                    vm.stats.fullReclaimedBytes = fullTotalReclaimed * 1024;
                    vm.stats.totalTime = VIUtil.calculateRunTime(Math.ceil((totalMinorTime + totalFullTime) * 1000));
                    vm.stats.minorTime = VIUtil.calculateRunTime(Math.ceil((totalMinorTime) * 1000));
                    vm.stats.fullTime = VIUtil.calculateRunTime(Math.ceil((totalFullTime) * 1000));
                    vm.cumuTimeData[0][1] = totalMinorTime;
                    vm.cumuTimeData[1][1] = totalFullTime;
                    vm.avgTimeData[0].values[0][1] = (totalMinorTime / minorCount).toFixed(2);
                    vm.avgTimeData[0].values[1][1] = ((totalFullTime / fullCount) || 0).toFixed(2);
                    vm.reclaBytesData[0].values[0][1] = minorTotalReclaimed * 1024;
                    vm.reclaBytesData[0].values[1][1] = fullTotalReclaimed * 1024;
                    youngPeak = youngPeak * 1024;
                    oldPeak = oldPeak * 1024;
                    vm.kpi.avgTime = Math.round(totalGCTime * 1000 / data.length);
                    var avgTime = vm.kpi.avgTime / 1000;
                    vm.stats.avgTime = vm.kpi.avgTime + ' ms';
                    vm.stats.avgTimeStdDev = Math.round(Math.sqrt(s / (j - 1)) * 1000) + ' ms';
                    vm.stats.minorAvgStdDev = Math.round(Math.sqrt(ms / (mj - 1)) * 1000) + ' ms';
                    vm.stats.fullAvgStdDev = Math.round(Math.sqrt(fs / (fj - 1)) * 1000) + ' ms';
                    /*
				Math.round(d3.deviation(data, function(x) {
                            return x[2];
                        }) * 1000) + 'ms';
			*/
                    vm.stats.minTime = Math.round(chooseOne(minorMinTime, fullMinTime, false) * 1000) + ' ms';
                    vm.stats.maxTime = VIUtil.calculateRunTime(Math.round(chooseOne(minorMaxTime, fullMaxTime, true) * 1000));
                    vm.stats.minorMin = Math.round(minorMinTime * 1000) + ' ms';
                    vm.stats.minorMax = VIUtil.calculateRunTime(Math.round(minorMaxTime * 1000));
                    vm.stats.minorAvg = Math.round(totalMinorTime * 1000 / minorCount) + ' ms';
                    vm.stats.fullMin = Math.round(fullMinTime * 1000) + ' ms';
                    vm.stats.fullMax = VIUtil.calculateRunTime(Math.round(fullMaxTime * 1000));
                    vm.stats.fullAvg = Math.round(totalFullTime * 1000 / fullCount) + ' ms';
                    if (data.length > 1) {
                        vm.stats.intervalAvgTime = VIUtil.calculateRunTime(totalDateDistance / (data.length - 1));
                    }

                    if (minorCount > 1) {
                        vm.stats.minorIntervalAvg = VIUtil.calculateRunTime(minorDateDistance / (minorCount - 1));
                    }
                    if (fullCount > 1) {
                        vm.stats.fullIntervalAvg = VIUtil.calculateRunTime(fullDateDistance / (fullCount - 1));
                    }

                    vm.kpi.maxTime = (Math.ceil(maxGCTime * 1000));
                    vm.kpi.maxTimeStr = VIUtil.calculateRunTime(vm.kpi.maxTime);

                    var durations = [];
                    for (var j=0;j<durationRange.length;j++) {
			    var curD = durationRange[j];
                        var cs = durationStats[curD];
                        if (cs && cs.count > 0) {
                            durations.push({
				    duration: cs.name+(curD==MaxDuration?(vm.kpi.maxTime / 1000).toFixed(1):''),
                                gcCount: cs.count,
                                percentage: Math.ceil(cs.count * 10000 / data.length) / 100
                            });
                        }

                    }

                    vm.kpi.durations = durations;
                    vm.kpiData = [];
                    vm.kpiData.push({
                        'color': colors[0],
                        'values': vm.kpi.durations.map(function(x) {

                            return {

                                'label': x.duration + ' secs',
                                'value': x.percentage
                            };
                        })
                    });

                    vm.heapSize = [{
                        'name': 'Young Generation',
                        'allocated': youngGen,
                        'peak': youngPeak
                    }, {
                        'name': 'Old Generation',
                        'allocated': oldGen,
                        'peak': oldPeak
                    }];

                    vm.jvmHeapData = [{
                        'key': 'Young Generation',
                        'color': colors[0],
                        'values': [{
                            label: 'allocated',
                            value: youngGen
                        }, {
                            label: 'peak usage',
                            value: youngPeak
                        }]

                    }, {
                        'key': 'Old Generation',
                        'color': colors[1],
                        'values': [{
                            label: 'allocated',
                            value: oldGen
                        }, {
                            label: 'peak usage',
                            value: oldPeak
                        }]

                    }];
                    if (permGen > 0) {
                        vm.heapSize.push({
                            'name': 'Perm Generation',
                            'allocated': permGen,
                            'peak': permPeak
                        });
                        vm.jvmHeapData.push({
                            'key': 'Perm Generation',
                            'color': colors[2],
                            'values': [{
                                label: 'allocated',
                                value: permGen
                            }, {
                                label: 'peak usage',
                                value: permPeak
                            }]

                        });

                    }

                    gcDatas = data;
                    delete vm.interactiveGraphData;
                    switchToHeapAfterGC(data);
                    if (isSeleFirst) {
                        vm.switchGraph(vm.operations[1]);
                        isSeleFirst = false;
                    } else {
                        vm.switchGraph(vm.operations[0]);
                        isSeleFirst = true;

                    }


                }



                vm.alloTip = "表明每代的分配大小.因数据来源于GC log，可能和JVM配置的参数(i.e. –Xmx, -Xms,…)一致或不一致.例如，应用设置 total heap size为2gb,然而运行时，如果JVM只分配1gb,那么在报告里将只会显示为1gb";
                vm.peakTip = "每代内存的峰值,一般峰值不会超过分配的大小,不过在少数情况峰值会超过分配值.特别在G1 GC里";

                vm.statisticTip = "基于'实时'数据的统计汇总";

                vm.jvmHeapOptions = {
                    chart: {
                        type: 'multiBarHorizontalChart',
                        margin: {
                            top: 20,
                            right: 80,
                            left: 80,
                            bottom: 30
                        },
                        height: 300,
                        showLegend: true,
                        x: function(d) {
                            return d.label;
                        },
                        y: function(d) {
                            return d.value;
                        },
                        showControls: true,
                        showValues: true,
                        duration: 500,
                        stacked: true,
                        xAxis: {
                            showMaxMin: false
                        },
                        yAxis: {
                            tickFormat: function(d) {
                                return VIUtil.formatBytes(d);
                            }
                        }
                    }
                };

                var colors = ['#d62728', '#1f77b4', 'purple'];


                vm.throughputTip = "处理真实请求占总处理时间的百分比. 高百分比是一个好迹象,表明GC开销低. 应致力达到高吞吐量";

                vm.avgGCTimeTip = "GC平均耗费时间";
                vm.maxGCTimeTip = "GC最长耗费时间";



                vm.kpiOptions = {
                    chart: {
                        type: 'multiBarHorizontalChart',
                        margin: {
                            top: 0,
                            right: 80,
                            left: 80,
                            bottom: 50
                        },
                        showLegend: false,
                        height: 300,
                        x: function(d) {
                            return d.label;
                        },
                        y: function(d) {
                            return d.value;
                        },
                        showControls: false,
                        showValues: true,
                        duration: 500,
                        stacked: true,
                        xAxis: {
                            showMaxMin: false
                        },
                        yAxis: {
                            tickFormat: function(d) {
                                return d + '%';
                            }
                        }
                    }
                };
                vm.switchGraph = function(opr) {

                    vm.seleOpr.selected = false;
                    opr.selected = true;
                    vm.seleOpr = opr;
                    vm.interactiveGraphOptions.chart.showLegend = true;
                    delete vm.interactiveGraphOptions.chart.callback;
                    vm.interactiveGraphOptions.chart.yAxis.tickFormat = function(d) {
                        return d3.format(',.0f')(d / 1024);
                    };
                    switch (opr.name) {
                        case 'Heap after GC':
                            switchToHeapAfterGC(gcDatas);
                            break;
                        case 'Heap before GC':
                            vm.interactiveGraphOptions.chart.type = 'lineChart';
                            vm.interactiveGraphOptions.chart.showLegend = false;
                            vm.interactiveGraphOptions.title.text = 'Heap before GC';
                            vm.interactiveGraphOptions.chart.yAxis.axisLabel = 'Heap Size(mb)';
                            vm.interactiveGraphOptions.chart.callback = function(chart) {
                                d3.select(chart.container).selectAll('path.nv-point').attr('class', function(d) {

                                    var oldClass = d3.select(this).attr('class') || '';
                                    if (d[0][1]) {
                                        //d3.select(this).style('stroke-width', '10').style('stroke', '#89ff98');
                                        return oldClass + ' fullgc-mark';
                                    } else {
                                        return oldClass;
                                    }
                                });
                            };
                            vm.interactiveGraphOptions.chart.x = function(x) {
                                return x[0];
                            };
                            vm.interactiveGraphOptions.chart.y = function(x) {
                                return x[6];
                            };
                            vm.interactiveGraphData = [{
                                "key": 'before GC',
                                'area': true,
                                'values': gcDatas
                            }];
                            break;
                        case 'GC Duration':
                            vm.interactiveGraphOptions.chart.type = 'scatterChart';
                            vm.interactiveGraphOptions.chart.color = colors;
                            vm.interactiveGraphOptions.chart.scatter = {
                                onlyCircles: false
                            };
                            vm.interactiveGraphOptions.title.text = 'GC Duration';
                            vm.interactiveGraphOptions.chart.yAxis.axisLabel = 'Time(secs)';
                            delete vm.interactiveGraphOptions.chart.x;
                            delete vm.interactiveGraphOptions.chart.y;

                            vm.interactiveGraphOptions.chart.yAxis.tickFormat = function(d) {
                                return d3.format(',.3f')(d);
                            };

                            vm.interactiveGraphData = [{
                                "key": 'Young GC',
                                'values': gcDatas.filter(function(x) {

                                    return !x[1];
                                }).map(function(x) {
                                    var duration = x[2];
                                    return {
                                        'x': x[0],
                                        'y': duration,
                                        'shape': 'square',
                                        'size': duration
                                    };

                                })
                            }, {
                                "key": 'full GC',
                                'values': gcDatas.filter(function(x) {

                                    return x[1];
                                }).map(function(x) {
                                    var duration = x[2];
                                    return {
                                        'x': x[0],
                                        'y': duration,
                                        'shape': 'triangle-up',
                                        'size': duration
                                    };

                                })
                            }];
                            break;
                        case 'Reclaimed Bytes':
                            vm.interactiveGraphOptions.title.text = 'Reclaimed Bytes';
                            vm.interactiveGraphOptions.chart.yAxis.axisLabel = '(mb)';
                            vm.interactiveGraphOptions.chart.type = 'scatterChart';
                            vm.interactiveGraphOptions.chart.color = colors;
                            vm.interactiveGraphOptions.chart.forceY = [0];
                            vm.interactiveGraphOptions.chart.scatter = {
                                onlyCircles: false
                            };

                            delete vm.interactiveGraphOptions.chart.x;
                            delete vm.interactiveGraphOptions.chart.y;


                            vm.interactiveGraphData = [{
                                "key": 'Young GC',
                                'values': gcDatas.filter(function(x) {

                                    return !x[1];
                                }).map(function(x) {
                                    var reclaimedBytes = x[3] - x[4];
                                    return {
                                        'x': x[0],
                                        'y': reclaimedBytes,
                                        'shape': 'square',
                                        'size': reclaimedBytes
                                    };

                                })
                            }, {
                                "key": 'full GC',
                                'values': gcDatas.filter(function(x) {

                                    return x[1];
                                }).map(function(x) {
                                    var reclaimedBytes = x[3] - x[4];
                                    return {
                                        'x': x[0],
                                        'y': reclaimedBytes,
                                        'shape': 'triangle-up',
                                        'size': reclaimedBytes
                                    };

                                })
                            }];

                            break;
                        case 'Young Gen':
                            vm.interactiveGraphOptions.chart.type = 'lineChart';
                            vm.interactiveGraphOptions.title.text = 'Young Gen';
                            vm.interactiveGraphOptions.chart.yAxis.axisLabel = '(mb)';
                            vm.interactiveGraphOptions.chart.x = function(x) {
                                return x[0];
                            };
                            vm.interactiveGraphOptions.chart.y = function(x) {
                                return x[1];
                            };

                            vm.interactiveGraphData = [{
                                "key": 'allocated space',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[5]];

                                })

                            }, {
                                "key": 'before GC',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[3]];

                                })
                            }, {
                                "key": 'after GC',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[4]];

                                })
                            }];
                            break;
                        case 'Old Gen':
                            vm.interactiveGraphOptions.title.text = 'Old Gen';
                            vm.interactiveGraphOptions.chart.yAxis.axisLabel = '(mb)';
                            vm.interactiveGraphOptions.chart.x = function(x) {
                                return x[0];
                            };
                            vm.interactiveGraphOptions.chart.y = function(x) {
                                return x[1];
                            };

                            vm.interactiveGraphData = [{
                                "key": 'allocated space',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[11]];

                                })

                            }, {
                                "key": 'before GC',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[9]];

                                })
                            }, {
                                "key": 'after GC',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[10]];

                                })
                            }];
                            break;
                        case 'A & P':
                            vm.interactiveGraphOptions.chart.type = 'lineChart';
                            vm.interactiveGraphOptions.title.text = 'Allocation & Promotion';
                            vm.interactiveGraphOptions.chart.yAxis.axisLabel = '(mb)';
                            vm.interactiveGraphOptions.chart.x = function(x) {
                                return x[0];
                            };
                            vm.interactiveGraphOptions.chart.y = function(x) {
                                return x[1];
                            };
                            vm.interactiveGraphOptions.chart.yAxis.tickFormat = function(d) {
                                return d3.format(',.3f')(d / 1024.0);
                            };
                            vm.interactiveGraphData = [{
                                "key": 'allocated objects size',
                                "values": gcDatas.map(function(x) {
                                    return [x[0], x[6]];
                                })
                            }, {
                                'key': 'Promoted (Young -> Old) objects size',
                                'values': gcDatas.map(function(x) {
                                    var promo = x[10] - x[9];
                                    return [x[0], promo > 0 ? promo : 0];
                                })
                            }];
                            break;


                    }
                };




                vm.oldGen = {
                    'allocatedSpace': gcDatas.map(function(x) {
                        return [x[0], x[8] - x[5]];

                    }),
                    'beforeGC': gcDatas.map(function(x) {
                        return [x[0], x[6] - x[3]];

                    }),
                    'afterGC': gcDatas.map(function(x) {
                        return [x[0], x[7] - x[4]];

                    })
                };


                vm.reclaBytesOptions = {
                    chart: {
                        type: 'discreteBarChart',
                        height: 300,
                        margin: {
                            top: 20,
                            right: 20,
                            bottom: 60,
                            left: 55
                        },
                        x: function(d) {
                            return d[0];
                        },
                        y: function(d) {
                            return d[1];
                        },
                        yAxis: {
                            tickFormat: function(d) {
                                return VIUtil.formatBytes(d);
                            }
                        },
                        showValues: true,
                        valueFormat: function(d) {
                            return VIUtil.formatBytes(d);
                        },
                        duration: 500
                    },
                    title: {
                        enable: true,
                        text: 'Reclaimed Bytes(GB)'
                    }
                };


                vm.avgTimeOptions = angular.copy(vm.reclaBytesOptions);
                vm.avgTimeOptions.title.text = 'GC Average Time(secs)';
                vm.avgTimeOptions.chart.valueFormat = function(d) {
                    return d;
                };
                vm.avgTimeOptions.chart.yAxis.tickFormat = function(d) {
                    return d + ' secs';
                };

                vm.cumuTimeOptions = angular.copy(vm.avgTimeOptions);
                vm.cumuTimeOptions.title.text = 'GC cumulative Time(secs)';
                vm.cumuTimeOptions.chart.type = 'pieChart';

                vm.cumuTimeOptions.chart.tooltip = {
                    contentGenerator: function(e) {
                        var series = e.series[0];
                        var header =
                            "<thead>" +
                            "<tr>" +
                            "<td class='legend-color-guide'><div style='background-color: " + series.color + ";'></div></td>" +
                            "<td class='key'><strong>" + series.key + "</strong></td>" +
                            "<td class='key'><strong>" + series.value.toFixed(2) + " secs</strong></td>" +
                            "</tr>" +
                            "</thead>";

                        return "<table>" +
                            header +
                            "</table>";
                    }
                };


            });
})();
