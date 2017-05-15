            function($element, $scope, serviceInfo, $stateParams, $timeout, $window, toastr) {
                var vm = {};
                var timer;
                var stepsChart;
                $scope.vm = vm;

                vm.maxHeight = ($window.innerHeight - 300) + 'px';
                var resizeFun = function() {
                    $scope.$apply(function() {
                        vm.maxHeight = ($window.innerHeight - 300) + 'px';
                    });
                };
                angular.element($window).on('resize', resizeFun);
                $scope.$on('$destroy', function(e) {
                    angular.element($window).off('resize', resizeFun);
                    if (timer !== null)
                        $timeout.cancel(timer);
                });

                serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
                    vm.data = data;
                    //Uninitiated,Running,Success,Failure

                    vm.statusClass = 'status-running';
                    var urlPattern = /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)/g;
                    vm.formatMsg = function(m) {
                        return m.replace(/\n/g, "<br>").replace(urlPattern, function(m) {
                            return '<a target="_blank" href="' + m + '">' + m + '</a>';
                        });
                    };
                    vm.colorMsg = function(m) {
                        if (m.indexOf('[ERROR]') >= 0) {
                            return 'status-failed';
                        } else if (m.indexOf('[WARN]') >= 0) {
                            return 'status-warn';
                        }

                    };
                    switch (data.status) {
                        case 'Success':
                            vm.statusClass = 'status-normal';
                            break;
                        case 'Failure':
                            vm.statusClass = 'status-failed';
                            break;
                    }

                    //nv.addGraph(function() {
                    stepsChart = nv.models.stepsChart();

                    stepsChart.currentIndex(data.currentPluginIndex).currentStatus(data.status);
                    d3.select('svg')
                        .datum(data.infos)
                        .call(stepsChart);

                    nv.utils.windowResize(stepsChart.update);
                    //   return stepsChart;
                    //});
                    if (data.status == 'Running') {
                        timer = $timeout(updateData, 1000);
                    }

                });

                function updateData() {

                    serviceInfo.doComponentMethod($stateParams.id, 'getLastStatus', {
                        'req': {
                            'msgIndex': vm.data.messages.length
                        }
                    }).then(function(data) {

                        switch (data['status']) {
                            case 'Success':
                                vm.statusClass = 'status-normal';
                                break;
                            case 'Failure':
                                vm.statusClass = 'status-failed';
                                break;
                        }
                        vm.data.status = data.status;
                        vm.data.cost = data.cost;
                        vm.data.currentPluginIndex = data.currentPluginIndex;

                        stepsChart.updateStatus(data.currentPluginIndex, data.status);
                        angular.forEach(data.messages, function(value) {
                            vm.data.messages.push(value);
                            var toastType = 'info';
                            if (value.length > 3) {
                                if (value.indexOf('[WARN]') >= 0) {
                                    toastType = 'warning';
                                } else if (value.indexOf('[ERROR]') >= 0) {
                                    toastType = 'error';
                                    value = value.substr(0, value.indexOf('\r\n'));
                                }
                                toastr[toastType](value);
                            }
                        });

                        if (data.status == 'Running') {
                            timer = $timeout(updateData, 1000);
                        }

                    });

                }


            }
