            function($element, $compile, $document, $scope, serviceInfo, $timeout) {

                $scope.vm={};
		var REFRESHINTERVAL=1000;
		var lastUpdateTime;
                var vm = $scope.vm;
                var MB = 1024 * 1024;
                var GB = 1024 * MB;
                vm.startDate = d3.time.format('%Y-%m-%d %H:%M:%S')(new Date());
                vm.requestOnready = function(s, e) {
                    vm.requestChart = s;
                    s.chart.lines1.duration(0);
                    s.chart.bars1.duration(0);
                    s.chart.bars2.duration(0);
                };

                vm.threadData = [{
                    values: [],
                    key: 'current thread',
                    color: '#2ca02c'
                }, {
                    values: [],
                    key: 'busy thread',
                    color: '#ff7f0e'
                }];
                vm.requestData = [{
                    values: [],
                    type: 'line',
                    yAxis: 1,
                    yDomain: [0, 100],
                    key: 'request count',
                    color: '#2ca02c'
                }, {
                    values: [],
                    type: 'line',
                    yAxis: 1,
                    yDomain: [0, 100],
                    key: 'error count',
                    color: '#ff7f0e'
                }, {
                    values: [],
                    type: 'bar',
                    yAxis: 2,
                    yDomain: [0, 100],
                    key: 'avg cost',
                    color: 'purple'
                }];
                vm.requestBytesData = [{
                    values: [],
                    key: 'bytes sent',
                    color: '#2ca02c'
                }, {
                    values: [],
                    key: 'bytes recevied',
                    color: '#ff7f0e'
                }];

                for (var i = 0; i < 60; i++) {

                    for (var k = 0; k < 2; k++) {

                        vm.threadData[k].values.push({
                            x: i,
                            y: 0
                        });
                        vm.requestData[k].values.push({
                            x: i,
                            y: 0
                        });
                        vm.requestBytesData[k].values.push({
                            x: i,
                            y: 0
                        });
                    }
                    vm.requestData[2].values.push({
                        x: i,
                        y: 0
                    });
                }

                var isInit = false;
                var timer;
                vm.requestOptions = {
                    chart: {
                        type: 'multiChart',
                        height: 200,
                        duration: 10,
                        xAxis: {

                            tickFormat: function(d) {

                                return d3.time.format('%H:%M:%S')(new Date(lastUpdateTime - (60 - d) * REFRESHINTERVAL));
                            }
                        },
                        yAxis1: {

                            axisLabel: 'count',
                            tickFormat: function(d) {
                                if (d > 1000000000)
                                    return d3.format('#,.1f')(d / 1000000000) + 'g';
                                else if (d > 1000000)
                                    return d3.format('#,.1f')(d / 1000000) + 'm';
                                else if (d > 1000)
                                    return d3.format('#,.1f')(d / 1000) + 'k';
                                else
                                    return d;
                            }
                        },
                        yAxis2: {

                            axisLabel: 'ms',
                            tickFormat: function(d) {
                                if (d > 1000000000)
                                    return d3.format('#,.1f')(d / 1000000000) + 'g';
                                else if (d > 1000000)
                                    return d3.format('#,.1f')(d / 1000000) + 'm';
                                else if (d > 1000)
                                    return d3.format('#,.1f')(d / 1000) + 'k';
                                else
                                    return d3.format('.1f')(d);
                            }
                        },
                        tickFormat: function(d) {
                            return d3.format('.02f')(d);
                        },
                        useInteractiveGuideline: true
                    }
                };
                vm.requestBytesOptions = {
                    chart: {
                        type: 'multiBarChart',
                        height: 200,
                        duration: 10,
                        stacked: true,
                        xAxis: {

                            tickFormat: function(d) {

                                return d3.time.format('%H:%M:%S')(new Date(lastUpdateTime - (60 - d) * REFRESHINTERVAL));
                            }
                        },
                        yAxis: {

                            tickFormat: function(d) {
                                if (d > GB)
                                    return d3.format('#,.1f')(d / GB) + 'GB';
                                else if (d > MB)
                                    return d3.format('#,.1f')(d / MB) + 'MB';
                                else if (d > 1024)
                                    return d3.format('#,.1f')(d / 1024) + 'KB';
                                else
                                    return d + 'b';
                            }
                        },
                        tickFormat: function(d) {
                            return d;
                        },
                        useInteractiveGuideline: true
                    }
                };
                vm.threadOptions = {
                    chart: {
                        type: 'lineChart',
                        yDomain: [0, 10],
                        height: 200,
                        xAxis: {

                            tickFormat: function(d) {

                                return d3.time.format('%H:%M:%S')(new Date(lastUpdateTime - (60 - d) * REFRESHINTERVAL));
                            }
                        },
                        yAxis: {

                            tickFormat: function(d) {
                                return d3.format('.0f')(d);
                            }
                        },
                        tickFormat: function(d) {
                            return d3.format('.0f')(d);
                        },
                        useInteractiveGuideline: true
                    }
                };

                var lastData = null;
                vm.maxRequestCount = 0;
                vm.formatBytes = function(d) {
                    if (d > GB)
                        return d3.format('#,.1f')(d / GB) + 'GB';
                    else if (d > MB)
                        return d3.format('#,.1f')(d / MB) + 'MB';
                    else if (d > 1024)
                        return d3.format('#,.1f')(d / 1024) + 'KB';
                    else
                        return d + 'b';
                };
                var threadBigPastSec = -1,
                    requestBigPastSec = -1,
                    bytesBigPastSec = -1;

                function updateData() {
                    serviceInfo.getComponentInfo('vi.tomcatmonitor').then(function(data) {
                        if (!isInit) {
                            isInit = true;
                        }
                        if (lastData == null) {
                            lastData = data;
                        }
			lastUpdateTime = new Date();

                        if (threadBigPastSec >= 0)
                            threadBigPastSec++;

                        vm.maxThreads = data.maxThreads;
                        vm.maxTime = d3.format('#,')(data.maxTime) + 'ms';

                        if (vm.threadOptions.chart.yDomain[1] / 1.2 < data.currentThreadCount) {
                            vm.threadOptions.chart.yDomain = [0, Math.round(data.currentThreadCount * 1.5)];
                            threadBigPastSec = 0;
                        }

                        var secRequestCount = data.requestCount - lastData.requestCount;

                        var secBytesSent = data.bytesSent - lastData.bytesSent;

                        if (threadBigPastSec > 61) {

                            vm.threadOptions.chart.yDomain = [0, Math.round(d3.max(vm.threadData[0].values, function(d) {
                                return d.y;
                            }) * 1.5)];
                            threadBigPastSec = 0;
                        }



                        vm.threadData[0].values.shift();
                        vm.threadData[0].values.push({
                            x: 0,
                            y: data.currentThreadCount
                        });
                        vm.threadData[1].values.shift();
                        vm.threadData[1].values.push({
                            x: 0,
                            y: data.currentThreadsBusy
                        });

                        vm.requestData[0].values.shift();

                        vm.requestData[0].values.push({
                            x: 0,
                            y: secRequestCount
                        });
                        if (secRequestCount > vm.maxRequestCount) {
                            vm.maxRequestCount = secRequestCount;
                        }

                        vm.requestData[1].values.shift();
                        vm.requestData[1].values.push({
                            x: 0,
                            y: data.errorCount - lastData.errorCount
                        });

                        vm.requestData[2].values.shift();
                        vm.requestData[2].values.push({
                            x: 0,
                            y: (data.processingTime - lastData.processingTime) / secRequestCount
                        });

                        vm.requestBytesData[0].values.shift();

                        vm.requestBytesData[0].values.push({
                            x: 0,
                            y: secBytesSent
                        });
                        if (secBytesSent > vm.maxBytesSent || vm.maxBytesSent == null) {
                            vm.maxBytesSent = secBytesSent;
                        }


                        vm.requestBytesData[1].values.shift();
                        var secBytesReceived = data.bytesReceived - lastData.bytesReceived;
                        vm.requestBytesData[1].values.push({
                            x: 0,
                            y: secBytesReceived
                        });
                        if (secBytesReceived > vm.maxBytesReceived || vm.maxBytesReceived == null) {
                            vm.maxBytesReceived = secBytesReceived;
                        }




                        for (var i = 0; i < 60; i++) {
                            vm.threadData[0].values[i].x = i;
                            vm.threadData[1].values[i].x = i;
                            vm.requestData[0].values[i].x = i;
                            vm.requestData[1].values[i].x = i;
                            vm.requestData[2].values[i].x = i;
                            vm.requestBytesData[0].values[i].x = i;
                            vm.requestBytesData[1].values[i].x = i;
                        }

                        lastData = data;
                        vm.nowDate = d3.time.format('%Y-%m-%d %H:%M:%S')(new Date());
                        timer = $timeout(updateData, 1000);


                    });
                }
                updateData();
                $scope.$on('$destroy', function(e) {
                    $timeout.cancel(timer);
                });
            }
