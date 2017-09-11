(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('DashboardController', function($document, $element, $scope, metricsService, $timeout, toastr,$location) {
            var metricsCount = 100;
            var vm = this;
            var observerId;
            var seleNamesTypes = [];
            vm.metrics = [];

	    vm.openSettings = function(){

		    $location.url('/settings/metrics');

	    };

            vm.metricName = function(m) {
                return /.*##\d*$/.test(m) ? m.substr(0, m.lastIndexOf('#') - 1) : m;
            };
            vm.onKeyDown = function(event) {
                if (!vm.isOpen && (event.keyCode == 38 || event.keyCode == 40)) {
                    vm.isOpen = true;
                }
                if (vm.isOpen) {
                    var listLen = vm.list.length;
                    switch (event.keyCode) {

                        case 13: //enter
                            vm.isOpen = false;
                            vm.selectName(vm.list[vm.selectedIndex]);
                            break;
                        case 38: //up

                            if (vm.selectedIndex) {

                                vm.selectedIndex--;

                                if (vm.selectedIndex <= 0)
                                    vm.selectedIndex = 0;

                            } else {
                                vm.selectedIndex = listLen - 1;
                            }
                            break;
                        case 40: //down
                            if (vm.selectedIndex >= 0) {

                                vm.selectedIndex++;

                                if (vm.selectedIndex >= listLen)
                                    vm.selectedIndex = 0;

                            } else {
                                vm.selectedIndex = 0;
                            }
                            break;

                    }
                }
            };

            vm.toggled = function() {
                if (vm.isOpen) {
                    metricsService.getNames(function(data) {

                        vm.metrics = data;
                    });
                }
                vm.isOpen = true;

            };
            var isInit = false;
            vm.chartData = [];
            vm.chartCostData = [];
            var percentiles = [];

            vm.monitor = function() {
                if (vm.enablePercentile && percentiles.length == 0) {
                    percentiles = [99.9, 95.0];
                    observerId = null;
                } else if (!vm.enablePercentile && percentiles.length > 0) {

                    percentiles = [];
                    observerId = null;
                }

                if (vm.selectedNames.length == 0) {

                    toastr.warning('you must select metrics first!', 'warning');
                    return;
                }

                var params = {
                    'names': vm.selectedNames,
                    'percentiles': percentiles
                };

                if (observerId) {
                    params['id'] = observerId;
                }

                metricsService.register(params, function(data) {
                    if (data instanceof VIException) {

                        toastr.error(data.Message, 'register failed');
                        return false;
                    }
                    observerId = data;
                    startDate = new Date();
                    if (timer)
                        $timeout.cancel(timer);

                    vm.chartData = [];
                    vm.chartCostData = [];
                    currentIndex = 0;
                    var i = 0;
                    for (var i = 0; i < vm.selectedNames.length; i++) {
                        var mName = vm.metricName(vm.selectedNames[i]);
                        vm.chartData.push({
                            values: [],
                            key: mName + '.count',
                            color: colors[i]
                        });
                        if (mName.length > 5 && mName.substr(-2, 2) == '##')
                            continue;
                        vm.chartCostData.push({
                            values: [],
                            key: mName + '.average',
                            color: colors[i]
                        });

                        angular.forEach(percentiles, function(val) {

                            vm.chartCostData.push({
                                values: [],
                                key: mName + '.' + val + 'Line',
                                color: colors[i]
                            });

                        });

                        vm.chartCostData.push({
                            values: [],
                            key: mName + '.max',
                            disabled: true,
                            color: colors[i]
                        });
                        vm.chartCostData.push({
                            values: [],
                            key: mName + '.min',
                            disabled: true,
                            color: colors[i]
                        });

                    }
                    updateData();
                });
            };

            vm.selectedNames = [];
            seleNamesTypes = [];
            vm.selectName = function(m) {
                if (vm.selectedNames.length > 4) {
                    toastr.warning('max metrics count is 5!');
                    return;
                }
                if (vm.selectedNames.indexOf(m) < 0) {
                    vm.selectedNames.push(m);
                    var tmp = m.split('##');
                    var val = 1;
                    switch (tmp.length > 1 ? tmp[1] : '-1') {
                        case '0': //second
                            val = 0.001;
                            break;
                        case '2':
                            val = 1000;
                            break;

                    }
                    seleNamesTypes.push(val);
                } else {

                    toastr.warning('already exist!');
                }
            };

            vm.removeName = function(m) {

                VIUtil.removeArrayItem(vm.selectedNames, m);
            };


            var REFRESHINTERVAL = 1000;
            var lastUpdateTime;
            var lastData, timer;
            var startDate;
            vm.startDate = d3.time.format('%Y-%m-%d %H:%M:%S')(new Date());

            vm.chartOptions = {
                chart: {
                    type: 'lineWithFocusChart',
                    height: 250,
                    forceX: [100],
                    forceY: [0,5],
                    noData: '',
                    xAxis: {
                        tickFormat: function(d) {
                            return d3.time.format('%H:%M:%S')(new Date(startDate - 0 + (d * REFRESHINTERVAL)));
                        }
                    },
                    x2Axis: {
                        tickFormat: function(d) {
                            return d3.time.format('%H:%M:%S')(new Date(startDate - 0 + (d * REFRESHINTERVAL)));
                        }
                    },
                    yAxis: {
                        axisLabel: 'Count',
                        tickFormat: function(d) {
                            return VIUtil.formatNumber(d);
                        }
                    },
                    tickFormat: function(d) {
                        return d;
                    },
                    useInteractiveGuideline: true
                }
            };

            vm.chartCostOptions = angular.copy(vm.chartOptions);

            vm.chartCostOptions.chart.yAxis.axisLabel = 'MilliSecond';
            vm.chartCostOptions.chart.forceY = [1];
            var colors = d3.scale.category20().range();
            var currentIndex = 0;
            var maxPointCount = 1800;


	    function getShowValue(factor,value){
		    return (factor < 1 ? VIUtil.noop : Math.round)(value) / factor;
	    }
            function updateData() {
                metricsService.getCurrent(observerId, function(data) {

                    if (data instanceof VIException) {
                        toastr.error(data.Message, 'error');
                        return;
                    }
                    var i = 0;

                    var yCount = vm.selectedNames.length;
                    for (var i, k = 0; i < yCount; i++, k++) {
                        var key = vm.selectedNames[i];
                        var sele = vm.chartData[i];
                        var factor = seleNamesTypes[i];

                        var val = 0;
                        var avergeCost, maxCost, minCost = 0;
                        var percentileValues = [];
                        if (data && data[key]) {
                            var dataItem = data[key];
                            val = dataItem.count;
                            avergeCost = getShowValue(factor,dataItem.total / dataItem.count);
                            maxCost = getShowValue(factor,dataItem.max);
                            minCost =  getShowValue(factor,dataItem.min);
                            percentileValues = dataItem.percentileValues;
                        }

                        if (!sele.values) return;

                        sele.values.push({
                            x: currentIndex,
                            y: val
                        });

                        if (key.length > 5 && key.substr(-2, 2) == '##') {
                            k--;
                            continue;
                        }

                        var costSele = vm.chartCostData[k * 3];


                        angular.forEach(percentiles, function(val, index) {
                            vm.chartCostData[k * 3 + index + 1].values.push({
                                x: currentIndex,
                                y:getShowValue(factor, percentileValues[index])
                            });

                        });

                        var maxSele = vm.chartCostData[k * 3 + 1 + percentiles.length];
                        var minSele = vm.chartCostData[k * 3 + 2 + percentiles.length];
                        costSele.values.push({
                            x: currentIndex,
                            y: avergeCost
                        });
                        maxSele.values.push({
                            x: currentIndex,
                            y: maxCost
                        });
                        minSele.values.push({
                            x: currentIndex,
                            y: minCost
                        });
                        if (sele.values.length > maxPointCount) {

                            var reduceCount = maxPointCount / 2;
                            sele.values.splice(0, reduceCount);
                            costSele.values.splice(0, reduceCount);
                            maxSele.values.splice(0, reduceCount);
                            minSele.values.splice(0, reduceCount);
                            angular.forEach(percentiles, function(val, index) {
                                vm.chartCostData[k * 3 + index + 1].values.splice(0, reduceCount);

                            });
                        }

                    }
                    currentIndex++;



                    vm.nowDate = d3.time.format('%Y-%m-%d %H:%M:%S')(new Date());
                    if (currentIndex == 1) {
                        updateData();
                    } else {
                        timer = $timeout(updateData, REFRESHINTERVAL);
                    }

                });
            }
            $scope.$on('$destroy', function(e) {
                $timeout.cancel(timer);
            });
        });
})();
