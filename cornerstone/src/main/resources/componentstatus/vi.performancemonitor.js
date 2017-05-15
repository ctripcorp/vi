function($scope, serviceInfo, $timeout, configService, $uibModal) {

    var REFRESHINTERVAL = 1000;
    var monitorCount = 60;
    var heapMemChart;
    var historyMax = 5400;
    var vm = {};
    $scope.vm = vm;
    $scope.urls = {};
    configService.get('vi.host.history', function(data) {
        $scope.urls.hostHistory = data;
    });


    $scope.heapMemOnReady = function(s, e) {
        heapMemChart = s;
    };

    $scope.cpu = {
        load: 0
    };
    $scope.mem = {
        load: 0,
        size: 0,
    };
    $scope.cpuOptions = {
        chart: {
            type: 'lineChart',
            yDomain: [0, 100],
            height: 200,

            xAxis: {

                tickFormat: function(d) {

                    return d3.time.format('%H:%M:%S')(new Date(vm.lastUpdateTime - (monitorCount - d) * REFRESHINTERVAL));
                }
            },
            yAxis: {

                tickFormat: function(d) {
                    return d3.format('.02f')(d) + '%';
                }
            },
            tickFormat: function(d) {
                return d3.format('.02f')(d);
            },
            useInteractiveGuideline: true
        }
    };

    $scope.cpuData = [{
        values: [],
        key: 'system cpu',
        color: '#2ca02c'
    }, {
        values: [],
        key: 'process cpu',
        color: '#ff7f0e'
    }];

    $scope.memData = [{
        values: [],
        key: 'used physical memory',
        color: '#2ca02c'
    }];
    if ($ISLINUX) {
        $scope.memData.push({
            values: [],
            key: 'used memory',
            color: '#ff7f0e'
        });

    }


    $scope.heapMemData = [{
        values: [],
        type: 'line',
        yAxis: 1,
        key: 'used heap memory',
        color: '#2ca02c'
    }, {
        values: [],
        type: 'bar',
        yAxis: 2,
        key: 'minor gc count',
        color: 'orange'
    }, {
        values: [],
        type: 'bar',
        yAxis: 2,
        key: 'full gc count',
        color: 'red'
    }];
    $scope.nonHeapMemData = [{
        values: [],
        key: 'used nonheap memory',
        color: '#2ca02c'
    }];

    $scope.swapData = [{
        values: [],
        key: 'used swap space',
        color: '#2ca02c'
    }];

    $scope.diskOptions = {
        chart: {
            type: 'multiBarHorizontalChart',
            height: 200,
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
                axisLabel: 'Values',
                tickFormat: function(d) {
                    return d3.format(',.2f')(d) + "GB";
                }
            }
        }
    };
    $scope.diskUsed = [];
    $scope.diskFreed = [];
    $scope.diskData = [{
        "key": "used space",
        "color": "#d62728",
        "values": $scope.diskUsed
    }, {
        "key": "free space",
        "color": "#1f77b4",
        "values": $scope.diskFreed
    }];
    var sysCpuData = $scope.cpuData[0].values;
    var processCpuData = $scope.cpuData[1].values;
    var physicMemData = $scope.memData[0].values;
    var heapMemData = $scope.heapMemData[0].values;
    var mGcData = $scope.heapMemData[1].values;
    var fGcData = $scope.heapMemData[2].values;
    var nonHeapMemData = $scope.nonHeapMemData[0].values;
    var swapData = $scope.swapData[0].values;
    for (var i = 0; i < monitorCount; i++) {
        sysCpuData.push({
            x: 0,
            y: 0
        });
        processCpuData.push({
            x: 0,
            y: 0
        });
        physicMemData.push({
            x: 0,
            y: 0
        });
        heapMemData.push({
            x: 0,
            y: 0
        });
        mGcData.push({
            x: 0,
            y: 0
        });
        fGcData.push({
            x: 0,
            y: 0
        });
        nonHeapMemData.push({
            x: 0,
            y: 0
        });
        swapData.push({
            x: 0,
            y: 0
        });
        if ($ISLINUX) {
            $scope.memData[1].values.push({
                x: 0,
                y: 0
            });
        }
    }


    var isInit = false;
    var timer;
    var mGcCount = 0,
        fGcCount = 0;
    var specialIsReady = false;
    var cacheData = [];
    var monitorStartTime;

    vm.showAll = function(type, chartOption, chartData) {

        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'perfmonitor732.html',
            controller: function($scope, $uibModalInstance) {
                var vm = {};
                $scope.vm = vm;
                var historyOption = angular.copy(chartOption);
                historyOption.chart.height = 500;

                historyOption.chart.xAxis = {
                    tickFormat: function(d) {
                        return d3.time.format('%H:%M:%S')(new Date(monitorStartTime - 0 + (d * REFRESHINTERVAL)));
                    }
                };
                var historyData = angular.copy(chartData);

                switch (type) {
                    case 'cpu':
                        vm.title = 'CPU';
                        historyData[0].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[0]
                            };
                        });

                        historyData[1].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[1]
                            };
                        });
                        break;

                    case 'mem':
                        vm.title = '物理内存';
                        historyData[0].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[2]
                            };
                        });
                        if ($ISLINUX) {
                            historyData[1].values = cacheData.map(function(v, i) {
                                return {
                                    'x': i,
                                    'y': v[8]
                                };
                            });

                        }
                        break;

                    case 'heapMem':
                        vm.title = '堆内存';
                        historyData[0].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[3]
                            };
                        });
                        historyData[1].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[4]
                            };
                        });
                        historyData[2].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[5]
                            };
                        });
                        break;
                    case 'nonHeapMem':
                        vm.title = '非堆内存';
                        historyData[0].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[6]
                            };
                        });
                        break;
                    case 'swap':
                        vm.title = 'swap空间';
                        historyData[0].values = cacheData.map(function(v, i) {
                            return {
                                'x': i,
                                'y': v[7]
                            };
                        });
                        break;
                }

                vm.historyOption = historyOption;
                vm.historyData = historyData;
                $scope.close = function() {
                    $uibModalInstance.dismiss('cancel');
                };
            },
            size: 'lg',
        });
    };

    serviceInfo.loopWithPeriod($scope, 'vi.performancemonitor', REFRESHINTERVAL, function(data) {
        vm.lastUpdateTime = new Date();
        if (cacheData.length > historyMax) {
            cacheData.shift();
            monitorStartTime -= REFRESHINTERVAL;
        }

        var tmpData = [];
        $scope.cpu.load = (Math.round(data.systemCpuLoad * 10000)) / 100;
        tmpData.push($scope.cpu.load);

        $scope.cpu.processLoad = (Math.round(data.processCpuLoad * 10000)) / 100;
        tmpData.push($scope.cpu.processLoad);
        $scope.mem.size = data.totalPhysicalMemorySize;
        $scope.mem.load = Math.round(100 * (data.totalPhysicalMemorySize - data.freePhysicalMemorySize) / data.totalPhysicalMemorySize);
        $scope.mem.usedGB = ($scope.mem.load * $scope.mem.size / 107374182400);
        if ($ISLINUX) {
            $scope.mem.usedAvailableGB = (data.totalPhysicalMemorySize - data.availableMemory) / 1073741824;
        }
        $scope.currentThreadCount = data.currentThreadCount;
        $scope.peakThreadCount = data.peakThreadCount;
        $scope.daemonThreadCount = data.daemonThreadCount;
        $scope.beanCreatedThreadCount = data.beanCreatedThreadCount;
        $scope.runtime = data.runtime;
        $scope.processCpuTime = data.processCpuTime;
        $scope.availableProcessors = data.availableProcessors;
        $scope.diskUsed.length = 0;
        $scope.diskFreed.length = 0;
        angular.forEach(data.rootFiles, function(item) {
            $scope.diskUsed.push({
                label: item.path,
                value: (item.totalSize - item.availableSize) / 1073741824
            });
            $scope.diskFreed.push({
                label: item.path,
                value: item.availableSize / 1073741824
            });

        });

        if (!isInit) {
            vm.startDate = vm.lastUpdateTime;
            monitorStartTime = vm.startDate;
            $scope.memOptions = {
                chart: {
                    type: 'lineChart',
                    yDomain: [0, ($scope.mem.size / 1073741824).toFixed(2)],
                    height: 200,
                    //	    showXAxis:false,
                    //	    showYAxis:false,
                    tickFormat: function(d) {
                        return d3.format('.02f')(d);
                    },
                    xAxis: {

                        tickFormat: function(d) {

                            return d3.time.format('%H:%M:%S')(new Date(vm.lastUpdateTime - (monitorCount - d) * REFRESHINTERVAL));
                        }
                    },
                    yAxis: {

                        tickFormat: function(d) {
                            return d3.format('.02f')(d) + 'GB';
                        }
                    },
                    useInteractiveGuideline: true
                }
            };
            var tmp = angular.copy($scope.memOptions);
            tmp.chart.type = 'multiChart';
            tmp.chart.yAxis1 = {
                axisLabel: 'size'
            };
            tmp.chart.yAxis2 = {
                axisLabel: 'count'
            };
            tmp.chart.yDomain1 = [0, (data.heapMemoryUsage.max / 1048576).toFixed(2)];
            tmp.chart.yDomain2 = [0, 2];
            tmp.chart.yAxis1.tickFormat = function(d) {
                return Math.round(d) + 'MB';
            };
            $scope.heapMemOptions = tmp;

            tmp = angular.copy($scope.memOptions);
            delete tmp.chart.yDomain;
            tmp.chart.yAxis.tickFormat = function(d) {
                return d3.format('.02f')(d) + 'MB';
            };
            $scope.nonHeapMemOptions = tmp;

            tmp = angular.copy($scope.memOptions);
            tmp.chart.yDomain = [0, (data.totalSwapSpaceSize / 1073741824).toFixed(2)];
            tmp.chart.yAxis.tickFormat = function(d) {
                return d3.format('.02f')(d) + 'GB';
            };
            $scope.swapOptions = tmp;

            $scope.mem.sizeMB = Math.round($scope.mem.size / 1048576);
            $scope.mem.freeMB = Math.round(data.freePhysicalMemorySize / 1048576);
            $scope.mem.committedVMMB = Math.round(data.committedVirtualMemorySize / 1048576);

            $scope.appStartUpTime = data.appStartUpTime;
            $scope.os = data.os;
            isInit = true;
            mGcCount = data.minorGcCount;
            fGcCount = data.fullGcCount;
        }

        sysCpuData.shift();
        sysCpuData.push({
            y: $scope.cpu.load
        });
        processCpuData.shift();
        processCpuData.push({
            y: $scope.cpu.processLoad
        });

        physicMemData.shift();
        tmpData.push($scope.mem.usedGB.toFixed(2));
        physicMemData.push({
            y: tmpData[2]
        });

        heapMemData.shift();
        tmpData.push((data.heapMemoryUsage.used / 1048576).toFixed(2));
        heapMemData.push({
            y: tmpData[3]
        });
        mGcData.shift();
        tmpData.push(data.minorGcCount - mGcCount);
        mGcData.push({
            y: tmpData[4]
        });
        fGcData.shift();
        tmpData.push(data.fullGcCount - fGcCount);
        fGcData.push({
            y: tmpData[5]
        });

        fGcCount = data.fullGcCount;
        mGcCount = data.minorGcCount;

        nonHeapMemData.shift();
        tmpData.push((data.nonHeapMemoryUsage.used / 1048576).toFixed(2));
        nonHeapMemData.push({
            y: tmpData[6]
        });
        swapData.shift();
        tmpData.push(((data.totalSwapSpaceSize - data.freeSwapSpaceSize) / 1073741824).toFixed(2));
        swapData.push({
            y: tmpData[7]
        });

        if ($ISLINUX) {
            var availMem = $scope.memData[1].values;
            availMem.shift();
            tmpData.push(($scope.mem.usedAvailableGB).toFixed(2));
            availMem.push({
                y: tmpData[8]
            });
        }
        cacheData.push(tmpData);
        for (var i = 0; i < monitorCount; i++) {
            sysCpuData[i].x = i;
            processCpuData[i].x = i;
            physicMemData[i].x = i;
            heapMemData[i].x = i;
            mGcData[i].x = i;
            fGcData[i].x = i;
            nonHeapMemData[i].x = i;
            swapData[i].x = i;
            if ($ISLINUX) {
                $scope.memData[1].values[i].x = i;
            }
        }
        //$scope.cpuApi.update();
        //######################
        //timer = $timeout(updateData, REFRESHINTERVAL);
        if (!specialIsReady && heapMemChart.chart) {
            specialIsReady = true;
            heapMemChart.chart.lines1.duration(0);
            heapMemChart.chart.bars1.duration(0);
            heapMemChart.chart.bars2.duration(0);
        }

        return true;
    });
    /*
    $scope.$on('$destroy', function(e) {
        $timeout.cancel(timer);
    });
    */

}
