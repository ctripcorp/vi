(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('SettingsController', SettingsController);

    function SettingsController($scope, $cookies, codeService, toastr, configService, analyzerService, $stateParams, $rootScope) {
        var vm = this;
        var tabs = {
            'debug': 0,
            'metrics': 1
        };
        vm.debug = {};
        vm.metrics = {};
        vm.seleTab = tabs[$stateParams.tab || 'debug'];
        vm.debug.gitPrivateToken = $cookies.get('gitPrivateToken');
        vm.debug.allowDebug = $cookies.get('allowDebug') == 'true';
        codeService.getTraceKey(function(d) {
            vm.oldTraceIdKey = d;
            vm.debug.headerTraceIdKey = d;
        });
        configService.get('vi.git.api.url', function(url) {
            var gitUrl = url.substring(0, url.indexOf('/api/'));
            vm.debug.gitlabAccountUrl = gitUrl + '/profile/account';
        });
        vm.debug.setAllowDebug = function() {
            if (vm.debug.allowDebug) {
                codeService.enableDebug(function(data) {
                    if (data instanceof VIException) {
                        toastr.error('Message:' + data.Message + '\nType:' + data.Type);
                        vm.debug.allowDebug = false;
                    } else if (!data) {
                        toastr.warning('enable allow debug failed!!');
                        vm.debug.allowDebug = false;
                    }
                    vm.debug.save();
                });
            } else {
                vm.debug.save();
            }
        };

        $scope.$on('$destroy', function() {
            if (vm.oldTraceIdKey != vm.debug.headerTraceIdKey) {
                codeService.setTraceKey({
                    'key': vm.debug.headerTraceIdKey
                }, function(data) {
                    if (data instanceof VIException) {
                        toastr.error('Message:' + data.Message + '\nType:' + data.Type);
                    } else {
                        toastr.info('set header traceIdKey success!');
                    }
                });
            }
        });

        vm.debug.save = function() {
            var oldToken = $cookies.get('gitPrivateToken');
            var oldAllowDebug = $cookies.get('allowDebug');
            $cookies.put('gitPrivateToken', vm.debug.gitPrivateToken);
            if (vm.debug.gitPrivateToken && oldToken != vm.debug.gitPrivateToken) {
                $rootScope.$broadcast('$gitTokenChange');
            }
            $cookies.put('allowDebug', vm.debug.allowDebug);
            if (vm.debug.allowDebug != oldAllowDebug) {
                $rootScope.$broadcast('$allowDebugChange');
            }
        };

        vm.metrics.seleClasses = {};
        analyzerService.getAllModuleInfo(function(data) {
            vm.metrics.jars = data;
        });
        analyzerService.getNeedMetricsClasses(function(data) {
            vm.metrics.seleClasses = {};
            data.forEach(function(x) {
                vm.metrics.seleClasses[x] = {};
            });
        });
        vm.metrics.jarChange = function() {
            if (vm.metrics.seleJar) {
                analyzerService.listClasses(vm.metrics.seleJar, function(data) {
                    vm.metrics.jarClasses = data.map(function(x) {
                        return {
                            'name': x
                        };
                    });
                });
            }

        };

        vm.metrics.seleClass = function(item) {

            item.selected = !item.selected;
        };
        vm.metrics.addToSelect = function(val) {
            analyzerService.addClassForMetrics(val.name, function(data) {
                if (data instanceof VIException) {
                    toastr.error('Message:' + data.Message + '\nType:' + data.Type);
                    return;
                }
                if (!(val.name in vm.metrics.seleClasses)) {
                    vm.metrics.seleClasses[val.name] = {};
                }
            });

        };

        vm.metrics.removeSeleClass = function(val) {

            analyzerService.removeMetricsClass(val, function(data) {
                if (data instanceof VIException) {
                    toastr.error('Message:' + data.Message + '\nType:' + data.Type);
                    return;
                }
                delete vm.metrics.seleClasses[val];
            });
        };

    }
})();
