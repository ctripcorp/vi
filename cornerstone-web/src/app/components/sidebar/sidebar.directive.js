(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('acmeSidebar', acmeNavbar);

    /** @ngInject */
    function acmeNavbar() {
        var directive = {
            restrict: 'E',
            templateUrl: 'app/components/sidebar/sidebar.html',
            controller: SidebarController,
            controllerAs: 'vm',
            scope: {
                'change': '&onChange'
            },
            bindToController: true
        };

        return directive;


        /** @ngInject */
        function SidebarController($scope, $window, $rootScope, serviceInfo, $filter,$location,ScrollbarService) {

            var vm = this;
            $scope.isCollapse = false;

            vm.isLoadComponents = false;
            vm.subMenuHeight = ($window.innerHeight - 130) + 'px';
            vm.menuMaxHeight = ($window.innerHeight - 170) + 'px';

	    vm.openSettings = function(){

		    $location.url('/settings/');

	    };
	    var sidebarScrollbar;

        ScrollbarService.getInstance('sidebar-scrollbar').then(function(x) {
            sidebarScrollbar= x;
        });

            var resizeFun = function() {
                $scope.$apply(function() {
                    vm.subMenuHeight = ($window.innerHeight - 130) + 'px';
                    vm.menuMaxHeight = ($window.innerHeight - 170) + 'px';
                });
            };
            angular.element($window).on('resize', resizeFun);

            $scope.$on('$destroy', function(e) {
                angular.element($window).off('resize', resizeFun);
            });

            $scope.collapse = function() {
                $scope.isCollapse = !$scope.isCollapse;
                vm.change({
                    isCollapse: $scope.isCollapse
                });
            };

            vm.hoverMenu = function(m, index) {

                vm.currentHover = m;
                var t = 45;
                var bheight = 56;
                if ($scope.isCollapse) {
                    vm.subMenuLeft = '48px';
                    t = 32;
                    bheight = 32;
                } else {

                    vm.subMenuLeft = '102px';
                }

                if (index > 0) {
                    t += bheight * (index);
                }

		var ty=t-sidebarScrollbar.offset.y;

                vm.subMenuTop = ty + 'px';
            };

            serviceInfo.getComponentMeta().then(function(data) {
                data = $filter('orderBy')(data, 'name');
                bindComponentMenus(data);
                $rootScope.components = data.map(function(item) {

                    var href = '/component/' + (item.custom == 'true' ? 'custom' : 'detail') + '/' + item.id + '/' + item.name;
                    if (item.list == 'true' && item.custom != 'true') {

                        href = '/component/detail.list/' + item.id + '/' + item.name;
                    }

                    return {
                        name: item.name,
                        description: item.description,
                        href: href
                    };


                });
            });

            var bindComponentMenus = function(data) {

                if (!vm.isLoadComponents) {
                    vm.isLoadComponents = true;
                    if ($scope.menus) {
                        var index = $scope.menus.findIndex(function(x) {

                            return x.name === 'Component';
                        });
                        if (index < 0) return;
                        var componentMenu = $scope.menus[index];

                        componentMenu.submenu = data.map(function(x) {
                            var params = {};
                            params.id = x.id;
                            params.name = x.name;

                            return {
                                description: '(' + x.description + ')',
                                name: x.name,
                                state: (x.custom == 'true' ? 'Component-Custom(' : (x.list == 'true' ? 'Component-DetailList(' : 'Component-Detail(')) +
                                    JSON.stringify(params) + ')'
                            };
                        });


                    }
                }
            };

            $scope.$on('$stateChangeStart', function(e, to, toParams, from) {

                var fromState = to.name;
                var someFunc = function(x) {
                    return x.state == fromState;
                };
                if (to.name == 'empty') {
                    e.preventDefault();
                } else {
                    for (var i = 0; i < $scope.menus.length; i++) {

                        var m = $scope.menus[i];

                        if (m.state == fromState ||
                            (m.submenu && m.submenu.some(someFunc))) {
                            $rootScope.$broadcast('$menuChange', m);
                        }

                    }

                }

            });

            $scope.$on('$stateChangeSuccess', function(evt, next, nextParams, from) {

                var nowState = next.name;
                var fromState = from.name;
                var someFunc = function(x) {
                    return x.state == nowState;
                };

                for (var i = 0; i < $scope.menus.length; i++) {

                    var m = $scope.menus[i];

                    if (m.state == nowState ||
                        (m.submenu && m.submenu.some(someFunc))) {
                        $scope.selemenu(m);
                        return;
                    }

                }

            });


            $scope.selemenu = function(m) {

                if (!isNaN(m)) {
                    m = $scope.menus[0];
                }

                if ($scope.selectMenu) {
                    $scope.selectMenu.selected = false;
                }
                m.selected = true;
                $scope.selectMenu = m;

            };

            if (typeof $SMENU !== 'undefined') {
                $scope.menus = $SMENU;
            } else {
                $scope.menus = [{
                    name: 'Component',
                    icon: 'fa-codepen',
                    state: 'Component',
                }, {
                    name: 'Metrics',
                    icon: 'fa-tachometer',
                    state: 'Dashboard'
                }, {
                    name: 'Analyzer',
                    icon: 'fa-coffee',
                    state: 'empty',
                    submenu: [{
                        name: 'Jar dependency',
                        state: 'Analyzer-Jardependency'
                    },{
                        name: 'Self Check',
                        state: 'Analyzer-SelfCheck'
                    }, {
                        name: 'JVM Sampler',
                        state: 'Analyzer-JVMSampler'
                    }, {
                        name: 'VisualGC',
                        state: 'Analyzer-VisualGC'
                    }, {
                        name: 'GCLogAnalyzer',
                        state: 'Analyzer-GCLogAnalyzer'
                    }]
                }, {
                    name: 'Configuration',
                    icon: 'fa-cogs',
                    state: 'Configuration'
                }, {
                    name: 'Code',
                    icon: 'fa-git',
                    state: 'Code'
                }, {
                    name: 'FC',
                    icon: 'fa-toggle-on',
                    state: 'FC'
                }, {
                    name: 'Cache Manager',
                    icon: 'fa-database',
                    state: 'Cache'
                }, {
                    name: 'Thread dump',
                    icon: 'fa-camera',
                    state: 'ThreadDump'
                }, {
                    name: 'File Log',
                    icon: 'fa-file',
                    state: 'Log'
                }, {
                    name: 'Custom',
                    icon: 'fa-file-o',
                    state: 'fc'
                }];
            }

            $scope.selemenu(0);

        }
    }

})();
