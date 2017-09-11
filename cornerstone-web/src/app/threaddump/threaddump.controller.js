(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('ThreadDumpController', ThreadDumpController);

    function formatTrace(trace) {
        var methodDetail = 'Native method';
        if (trace.lineNumber > 0) {
            var ns = trace.declaringClass.substr(0, trace.declaringClass.lastIndexOf('.'));
            methodDetail = '<span jns="' + ns + '" ng-click="vm.viewSource($event)" class="link">' + trace.fileName + ':' + trace.lineNumber + '</span>';
        }
        return trace.declaringClass + '.' + trace.methodName + '(' + methodDetail + ')';
    }

    function ThreadModalController($scope, data, $uibModalInstance) {
        $scope.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.title = data.title;
        $scope.rawCollection = data.items;

        $scope.formatTrace = formatTrace;

        $scope.getStateTip = function(info) {

            return (info.lockName ? info.lockName + "上的" : "") + info.threadState;
        };

    }

    /** @ngInject */
    function ThreadDumpController($scope, toastr, $window, $element, $timeout, threadService, SiteCache, $uibModal,codeService,$rootScope) {
        var vm = this;
        vm.showDiagram = true;
        vm.maxDepth = 3;
        var REFRESHINTERVAL = 3000;
        var lastUpdateTime;

        vm.viewSource = function(event) {

            var $this = angular.element(event.currentTarget);
            var ns = $this.attr('jns');
            var name = $this.text();
	    codeService.viewCode(ns,name);
        };


        vm.threadDump = function(onlyDeadLock) {

            threadService.dump({
                maxDepth: vm.maxDepth,
                onlyDeadLock: onlyDeadLock || false
            }, function(data) {
                if (data == null && onlyDeadLock) {
                    toastr.info('no dead lock thread found.');
                    return;
                }
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'threadModal.html',
                    controller: ThreadModalController,
                    size: 'lg',
                    resolve: {
                        data: {
                            items: data,
                            title: (onlyDeadLock ? 'deadlock threads' : 'all threads')
                        }
                    }
                });
            });
        };
        var dumpContainerHeight = $window.outerHeight - 570;
        if (dumpContainerHeight < 300) {
            dumpContainerHeight = 300;
        }

        vm.needHighlight = function(str) {
            if (vm.highlightWords) {
                var words = vm.highlightWords.split(/\s+/);
                var pattern = new RegExp('(' + words.join(')|(') + ')', 'gi');
                return pattern.test(str);
            } else {
                return false;
            }
        };

        vm.fixedContainer = {
            'height': dumpContainerHeight + 'px'
        };

        vm.threadOptions = {
            chart: {
                type: 'lineChart',
                height: 200,
                xAxis: {

                    tickFormat: function(d) {

                        return d3.time.format('%H:%M:%S')(new Date(lastUpdateTime - (60 - d) * REFRESHINTERVAL));
                    }
                },
                yAxis: {

                    tickFormat: function(d) {
                        return d;
                    }
                },
                tickFormat: function(d) {
                    return d;
                },
                useInteractiveGuideline: true
            }
        };

        vm.threadData = [{
            values: [],
            key: 'current count',
            color: '#2ca02c'
        }, {
            values: [],
            key: 'daemon count',
            color: '#ff7f0e'
        }, {
            values: [],
            key: 'total started count',
            disabled: true,
            color: '#0066cc'
        }, {
            values: [],
            key: 'peak count',
            color: '#99cc00'
        }];
        vm.threadInfo = {};
        vm.collapse = function() {
            vm.showDiagram = !vm.showDiagram;
        };

        vm.selectedItem = null;

        vm.select = function(t) {
            if (vm.selectedItem) {
                vm.selectedItem.selected = false;
            }
            t.selected = true;
            vm.selectedItem = t;
            threadService.getDetail(t.id, {
                maxDepth: 100
            }, function(data) {
                vm.threadInfo = data;
            });

        };

        vm.formatTrace = formatTrace;

        vm.refreshThreads = function() {

            vm.totalCpu = 0;
            vm.threads = [];
            threadService.getAll(function(data) {

                vm.threads = data;
                vm.totalCpu = data.reduce(function(o1, o2) {
                    if (isNaN(o1)) {
                        return o1.cpuTime + o2.cpuTime;
                    } else {

                        return o1 + o2.cpuTime;
                    }
                });

            });
        };
        vm.refreshThreads();
        var currentCountData = vm.threadData[0].values;
        var daemonCountData = vm.threadData[1].values;

        for (var i = 0; i < 60; i++) {
            for (var ti = 0; ti < vm.threadData.length; ti++) {
                vm.threadData[ti].values.push({
                    x: i,
                    y: 0
                });
            }
        }


        var isInit = false;
        var timer;

        function updateData() {
            threadService.getStats(function(data) {


                lastUpdateTime = new Date();

                vm.currentThreadStats = data;
                vm.threadData[0].values.shift();
                vm.threadData[0].values.push({
                    y: data.currentThreadCount
                });
                vm.threadData[1].values.shift();
                vm.threadData[1].values.push({
                    y: data.daemonThreadCount
                });
                vm.threadData[2].values.shift();
                vm.threadData[2].values.push({
                    y: data.totalStartedThreadCount
                });
                vm.threadData[3].values.shift();
                vm.threadData[3].values.push({
                    y: data.peakThreadCount
                });

                for (var i = 0; i < 60; i++) {
                    for (var ti = 0; ti < vm.threadData.length; ti++) {
                        vm.threadData[ti].values[i]['x'] = i;
                    }
                }
                //$scope.cpuApi.update();
                //######################
                timer = $timeout(updateData, REFRESHINTERVAL);

            });
        }
        updateData();
        $scope.$on('$destroy', function(e) {
            $timeout.cancel(timer);
        });
    }
})();
