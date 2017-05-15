(function() {
    'use strict';

    angular
        .module('viModule')
        .config(routerConfig);

    /** @ngInject */
    function routerConfig($stateProvider, $urlRouterProvider, $controllerProvider) {

        var customComponentCache = {};
        var customPageCache = {};
        var states = $stateProvider
            .state('empty', {})
            .state('Configuration', {
                url: '/config',
                templateUrl: 'app/config/config.html',
                controller: 'ConfigController',
                controllerAs: 'vm'
            })
            .state('Dashboard', {
                url: '/dashboard',
                templateUrl: 'app/dashboard/dashboard.html',
                controller: 'DashboardController',
                controllerAs: 'vm'
            })
            .state('JVM-Sampler', {
                url: '/dashboard/trace',
                templateUrl: 'app/dashboard/trace/trace.html',
                controller: 'TraceController',
                controllerAs: 'vm'
            })
            .state('Component', {
                url: '/',
                templateUrl: 'app/component/component.html',
                controller: 'ComponentController',
                controllerAs: 'vm'
            })
            .state('Component-Detail', {
                url: '/component/detail/:id/:name',
                templateUrl: 'app/component/component.detail.html',
                controller: 'ComponentDetailController',
                controllerAs: 'vm'
            })
            .state('Component-DetailList', {
                url: '/component/detail.list/:id/:name',
                templateUrl: 'app/component/component.detail.list.html',
                controller: 'ComponentDetailListController',
                controllerAs: 'vm'
            })
            .state('Component-Custom', {
                url: '/component/custom/:id/:name',
                template: function($stateParams) {

                    return '<div ng-controller="' + $stateParams.id + '">' + customComponentCache[$stateParams.id + '.html'] + '</div>';

                },
                resolve: {
                    load: function($q, $stateParams, customComponentStatus) {

                        return customComponentStatus.getCustom($stateParams.id).then(function(data) {
                            customComponentCache[$stateParams.id + '.html'] = unescape(data.data.html);
                            var tmpFun = eval('({fun:' + data.data.js + '})');
                            $controllerProvider.register($stateParams.id, tmpFun.fun);

                        });


                    }

                },
                controllerAs: 'vm'
            })
            .state('FC', {
                url: '/fc',
                templateUrl: 'app/fc/fc.html',
                controller: 'FCController',
                controllerAs: 'vm'
            })
            .state('Log', {
                url: '/log',
                templateUrl: 'app/log/log.html',
                controller: 'LogController',
                controllerAs: 'vm'
            })
            .state('Log-Detail', {
                url: '/log/detail/:name/:size',
                templateUrl: 'app/log/log.detail.html',
                controller: 'LogDetailController',
                controllerAs: 'vm'
            })
            .state('ThreadDump', {
                url: '/threaddump',
                templateUrl: 'app/threaddump/threaddump.html',
                controller: 'ThreadDumpController',
                controllerAs: 'vm'
            })
            .state('Cache', {
                url: '/cache',
                templateUrl: 'app/cache/cache.html',
                controller: 'CacheController',
                controllerAs: 'vm'
            })
            .state('Analyzer-Jardependency', {
                url: '/analyzer/jardeps',
                templateUrl: 'app/analyzer/jardeps.html',
                controller: 'JardepsController',
                controllerAs: 'vm'
            })
            .state('Analyzer-JVMSampler', {
                url: '/analyzer/jvmsampler',
                templateUrl: 'app/analyzer/jvmsampler.html',
                controller: 'JvmSamplerController',
                controllerAs: 'vm'
            });

        if (typeof $SMENU !== 'undefined') {

            var templateFun = function($stateParams) {

                return '<div ng-controller="' + $stateParams.pageId + '">' + customPageCache[$stateParams.pageId + '.html'] + '</div>';

            };
            var resolveFun = {
                load: function($q, $stateParams, customPageService) {

                    $stateParams.pageId = 'custom-' + this.self.name;
                    $stateParams.stateId = this.self.name;
                    return customPageService.get(this.self.name).then(function(data) {
                        customPageCache[$stateParams.pageId + '.html'] = unescape(data.html);
                        var tmpFun = eval('({fun:' + data.js + '})');
                        $controllerProvider.register($stateParams.pageId, tmpFun.fun);

                    });
                }
            };

            var addStateFun = function(x) {
                if (!x.isBuildIn) {
                    states.state(x.state, {

                        url: '/page/' + x.state,
                        template: templateFun,
                        resolve: resolveFun

                    });
                }
            };

            angular.forEach($SMENU, function(x) {
                addStateFun(x);
                if (x.submenu) {
                    angular.forEach(x.submenu, addStateFun);

                }

            });
        }

        $urlRouterProvider.otherwise('/');
    }

})();
