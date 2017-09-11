(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('VisualGCController',
            function($element, $compile, $scope, $timeout, analyzerService) {
                var vm = this;
                var timer;
                var isInit = false;
                var KB = 1024;
                var tick = 0;
                var isMetaspace = false;

                var refreshData = function() {
                    analyzerService.getVMSnapshot(function(ds) {

				vm.permTitle = "Perm Gen";
                        if (!ds.permSize) {
				vm.permTitle = "Metaspace";
                            isMetaspace = true;
                            ds.permSize = ds.metaSize;
                            ds.permCapacity = ds.metaCapacity;
                            ds.permUsed = ds.metaUsed;

                        }
                        vm.lastUpdateTime = new Date();
                        if (!isInit) {
                            isInit = true;

                            vm.permOptions = angular.copy(vm.options);
                            vm.permOptions.chart.x = function(d, i) {
                                return i;
                            };
                            vm.permOptions.chart.yTickFormat = function(d) {
                                return VIUtil.formatBytes(d * KB, 'm');
                            };
                            vm.edenOptions = angular.copy(vm.permOptions);
                            vm.s0Options = angular.copy(vm.permOptions);
                            vm.s1Options = angular.copy(vm.permOptions);
                            vm.oldOptions = angular.copy(vm.permOptions);
                            vm.cloaderOptions = angular.copy(vm.permOptions);
                            vm.cloaderOptions.chart.yTickFormat = function(d) {
                                return d;
                            };
                            vm.startDate = vm.lastUpdateTime;
                            vm.permOptions.chart.yDomain = [0, Math.ceil(ds.permCapacity / KB)];
                            vm.edenOptions.chart.yDomain = [0, Math.ceil(ds.edenCapacity / KB)];
                            vm.oldOptions.chart.yDomain = [0, Math.ceil(ds.tenuredCapacity / KB)];
                            vm.s0Options.chart.yDomain = [0, Math.ceil(ds.survivor0Capacity / KB)];
                            vm.s1Options.chart.yDomain = [0, Math.ceil(ds.survivor1Capacity / KB)];

                        }
                        vm.tenuringThreshold = ds.tenuringThreshold;
                        vm.maxTenuringThreshold = ds.maxTenuringThreshold;
                        vm.desiredSurvivorSize = ds.desiredSurvivorSize;
                        vm.currentSurvivorSize = ds.survivor1Size;
                        if (ds.ageTableSizes) {
                            vm.histogramWidth = Math.floor(($element[0].clientWidth - 200) / ds.ageTableSizes.length) + 'px';
                            vm.histogram = ds.ageTableSizes.map(function(x) {
                                return Math.ceil((ds.desiredSurvivorSize - x) * 100 / ds.desiredSurvivorSize);
                            });
                        }
                        vm.permLeftPercent = Math.ceil((ds.permSize - ds.permUsed) * 100 / ds.permSize);
                        vm.oldLeftPercent = Math.ceil((ds.tenuredSize - ds.tenuredUsed) * 100 / ds.tenuredSize);
                        vm.edenLeftPercent = Math.ceil((ds.edenSize - ds.edenUsed) * 100 / ds.edenSize);
                        vm.s0LeftPercent = Math.ceil((ds.survivor0Capacity - ds.survivor0Used) * 100 / ds.survivor0Capacity);
                        vm.s1LeftPercent = Math.ceil((ds.survivor1Capacity - ds.survivor1Used) * 111 / ds.survivor1Capacity);
                        vm.oldTotalCompile = vm.totalCompile;
                        vm.totalCompile = ds.totalCompile;
                        vm.compileTime = (ds.totalCompileTime / ds.osFrequency).toFixed(3);
                        vm.classesLoaded = ds.classesLoaded;
                        vm.classesUnloaded = ds.classesUnloaded;
                        vm.classLoadTime = (ds.classLoadTime / ds.osFrequency).toFixed(3);
                        vm.oldGcEvents = vm.gcEvents;
                        vm.gcEvents = ds.edenGCEvents + ds.tenuredGCEvents;
                        vm.gcTimes = VIUtil.calculateRunTime((ds.edenGCTime + ds.tenuredGCTime) * 1000 / ds.osFrequency);
                        vm.lastGCCause = ds.lastGCCause;
                        vm.edenSize = VIUtil.formatBytes(ds.edenSize, 'm');
                        vm.edenCapacity = VIUtil.formatBytes(ds.edenCapacity, 'm');
                        vm.edenGCEvents = ds.edenGCEvents;
                        vm.edenGCTime = VIUtil.calculateRunTime(ds.edenGCTime * 1000 / ds.osFrequency);
                        vm.edenUsed = VIUtil.formatBytes(ds.edenUsed, 'm');
                        vm.survivor0Size = VIUtil.formatBytes(ds.survivor0Size, 'm');
                        vm.survivor0Capacity = VIUtil.formatBytes(ds.survivor0Capacity, 'm');
                        vm.survivor0Used = VIUtil.formatBytes(ds.survivor0Used, 'm');
                        vm.survivor1Size = VIUtil.formatBytes(ds.survivor1Size, 'm');
                        vm.survivor1Capacity = VIUtil.formatBytes(ds.survivor1Capacity, 'm');
                        vm.survivor1Used = VIUtil.formatBytes(ds.survivor1Used, 'm');

                        vm.tenuredSize = VIUtil.formatBytes(ds.tenuredSize, 'm');
                        vm.tenuredCapacity = VIUtil.formatBytes(ds.tenuredCapacity, 'm');
                        vm.tenuredGCEvents = ds.tenuredGCEvents;
                        vm.tenuredGCTime = VIUtil.calculateRunTime(ds.tenuredGCTime * 1000 / ds.osFrequency);
                        vm.tenuredUsed = VIUtil.formatBytes(ds.tenuredUsed, 'm');
                        vm.permSize = VIUtil.formatBytes(ds.permSize, 'm');
                        vm.permCapacity = VIUtil.formatBytes(ds.permCapacity, 'm');
                        vm.permUsed = VIUtil.formatBytes(ds.permUsed, 'm');
                        var totalCompileChange = 0,
                            gcEventChange = 0;
                        if (vm.oldTotalCompile) {
                            totalCompileChange = vm.totalCompile - vm.oldTotalCompile;
                        }
                        if (vm.oldGcEvents) {

                            gcEventChange = vm.gcEvents - vm.oldGcEvents;

                        }
                        vm.compileData[0].values.push(
                            [vm.compileData[0].values.length,
                                totalCompileChange
                            ]
                        );
                        vm.gcData[0].values.push(
                            [vm.gcData[0].values.length,
                                gcEventChange
                            ]
                        );

                        vm.cloaderData[0].values.push({
                            x: tick,
                            y: ds.classesLoaded
                        });
                        vm.edenData[0].values.push({
                            x: tick,
                            y: Math.ceil(ds.edenUsed / KB)
                        });
                        vm.permData[0].values.push({
                            x: vm.permData[0].values.length,
                            y: Math.ceil(ds.permUsed / KB)
                        });
                        vm.oldData[0].values.push({
                            x: vm.oldData[0].values.length,
                            y: Math.ceil(ds.tenuredUsed / KB)
                        });
                        vm.s0Data[0].values.push({
                            x: vm.s0Data[0].values.length,
                            y: Math.ceil(ds.survivor0Used / KB)
                        });
                        vm.s1Data[0].values.push({
                            x: vm.s1Data[0].values.length,
                            y: Math.ceil(ds.survivor1Used / KB)
                        });
                        tick++;

                        timer = $timeout(refreshData, 1000);
                    });
                };
                refreshData();

                $scope.$on('$destroy', function() {
                    $timeout.cancel(timer);
                });


                vm.options = {
                    chart: {
                        type: 'lineChart',
                        height: 80,
                        showLegend: false,
                        margin: {
                            top: 0,
                            right: 0,
                            left: 0,
                            bottom: 0
                        }
                    }
                };

                vm.compileData = [{
                    'key': 'count',
                    'bar': true,
                    'values': []
                }];

                vm.cloaderOptions = vm.options;
                vm.compileOptions = {
                    chart: {
                        type: 'historicalBarChart',
                        height: 80,
                        forceY: [3],
                        duration: 250,
                        showLegend: false,
                        x: function(d) {
                            return d[0];
                        },
                        y: function(d) {
                            return d[1];
                        },
                        margin: {
                            top: 0,
                            right: 0,
                            left: 0,
                            bottom: 0
                        },
                        tooltip: {
                            keyFormatter: function(d) {
                                return d3.time.format('%H:%M:%S')(new Date(vm.startDate - 0 + d * 1000));
                            }
                        }
                    }
                };
                vm.gcOptions = angular.copy(vm.compileOptions);
                vm.gcData = angular.copy(vm.compileData);
                vm.permData = [{
                    'area': true,
                    'values': []
                }];
                vm.oldData = [{
                    'area': true,
                    'values': []
                }];
                vm.s0Data = [{
                    'area': true,
                    'values': []
                }];
                vm.s1Data = [{
                    'area': true,
                    'values': []
                }];
                vm.edenData = [{
                    'area': true,
                    'values': []
                }];
                vm.cloaderData = [{
                    'area': true,
                    'values': []
                }];


            }
        );

})();
