(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('acmeNavbar', acmeNavbar);

    /** @ngInject */
    function acmeNavbar() {
        var directive = {
            restrict: 'E',
            templateUrl: 'app/components/navbar/navbar.html',
            scope: {
                creationDate: '='
            },
            controller: NavbarController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;

        /** @ngInject */
        function NavbarController($scope, SiteCache, $cookies, $uibModal, ApiService, $state, enterpriseService, $rootScope, appService) {
            var vm = this;
            var hostInfo = SiteCache.get('hostInfo');
            vm.loginUser = $cookies.get('vi-user');
            vm.isOriginIp = true;
            if (typeof $PORTALURL !== 'undefined') {
                vm.portalUrl = $PORTALURL;
            }
            enterpriseService.getHelp(function(data) {
                vm.links = data;
            });

            vm.tabs = {};
            vm.version = 'dev';
            if (typeof $VERSION !== 'undefined') {
                vm.version = $VERSION;
            }

            var seleIndex = null;

            vm.switch = function(index) {

                if (seleIndex !== null && vm.tabs[seleIndex]) {
                    vm.tabs[seleIndex].selected = false;
                }

                if (isNaN(index)) {
                    vm.tabs[index].selected = true;
                    seleIndex = index;
                }

                $rootScope.$broadcast('$switchTab', index);
            };

            vm.removeTab = function(index) {
                delete vm.tabs[seleIndex];
                seleIndex = 0;
                $rootScope.$broadcast('$removeTab', index);

            };

            $scope.$on('$viViewAdded', function(event, state, name) {

                if (seleIndex !== null && vm.tabs[seleIndex]) {
                    vm.tabs[seleIndex].selected = false;
                }

                vm.tabs[state + name] = {
                    'name': name,
                    'selected': true
                };
                seleIndex = state + name;

            });

            vm.changeServer = function() {

                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'viserversmodal.html',
                    controller: function($scope, $uibModalInstance, currentServer, enterpriseService) {

                        var vm = {};
                        $scope.vm = vm;
                        var poolServers = SiteCache.get('poolServers');
                        vm.selectedIdc = 'all';
                        vm.idcs = {
                            'all': 'all'
                        };
                        vm.idcFilter = function(m) {

                            if (vm.selectedIdc == 'all') return true;
                            return m.idc == vm.selectedIdc;
                        };
                        enterpriseService.getServers(function(data) {

                            for (var i = 0; i < data.length; i++) {
                                var item = data[i];

                                if (!(item.idc in vm.idcs))
                                    vm.idcs[item.idc] = item.idc;
                            }
                            vm.servers = data;
                            SiteCache.put('poolServers', vm.servers);
                            vm.hasContent = true;
                        });

                        vm.hostIp = currentServer.ip;
                        $scope.ok = function() {
                            var rtn = null;
                            if (vm.selectedRow) {
                                rtn = vm.selectedRow;

                            }
                            $uibModalInstance.close(rtn);
                        };
                        $scope.cancel = function() {
                            $uibModalInstance.dismiss('cancel');
                        };
                    },
                    size: 'lg',
                    resolve: {
                        currentServer: {
                            ip: vm.hostIp
                        }
                    }
                });

                vm.returnOrigin = function() {

                    var apiPath = location.pathname.substring(0, location.pathname.length - 10) + 'api';
                    var hostInfo = SiteCache.get('hostInfo');
                    ApiService.setEndpoint({
                        host: 'http://' + hostInfo.IP,
                        port: location.port,
                        path: apiPath
                    });

                    vm.hostName = hostInfo.HostName;
                    vm.hostIp = hostInfo.IP;
                    vm.isOriginIp = true;
                    $state.reload();
                };

                modalInstance.result.then(function(server) {
                    if (location.pathname.length < 10)
                        return;
                    var hostInfo = SiteCache.get('hostInfo');
                    var isFromProxy = false;
                    var port = location.port;
                    var pathName = location.pathname.replace(/\/(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})(:|\/)/g, function(match, m1, m2, offset) {
                        if (m1) {
                            isFromProxy = true;
                            return '/' + server.ip + m2;
                        }
                    });

                    var apiPath;
                    var FIXPATH = '/@in/api';
                    if (!isFromProxy) {
                        var sindex = server.url.indexOf('/');
                        if (sindex > 0) {
                            port = server.url.substr(0, sindex);
                            apiPath = '/' + server.url.substr(sindex + 1) + FIXPATH;
                        } else {
                            port = server.url;
                            apiPath = FIXPATH;
                        }

                    } else {
                        apiPath = '/' + server.ip + ':' + server.url + FIXPATH;
                    }

                    ApiService.setEndpoint({
                        host: 'http://' + (isFromProxy ? location.hostname : server.ip),
                        port: port,
                        path: apiPath
                    });
                    vm.hostName = server.name;
                    vm.hostIp = server.ip;
                    vm.isOriginIp = vm.hostIp == hostInfo.IP;
                    $state.reload();
                });
            };
            if (hostInfo) {
                vm.hostName = hostInfo.HostName;
                vm.hostIp = hostInfo.IP;
            } else {

                vm.hostName = $CHOSTNAME;
                vm.hostIp = $CIP;
            }

            $scope.$on('hostInfo.ready', function(event) {
                var data = SiteCache.get('hostInfo');
                vm.hostName = data.HostName;
                vm.hostIp = data.IP;

            });
            $scope.$on('appinfo.ready', function(event, data) {
                vm.appName = data.appName;
                vm.appid = data.appid;
                vm.appChineseName = data.appChineseName;
            });
            var currentMenu;
            $scope.$on('$menuChange', function(evt, data) {

                currentMenu = data;

            });

            $scope.$on('$stateChangeSuccess', function(evt, next, toParams) {

                var vName;
                var newState = next.name;
                if (newState != 'Log-Detail' && toParams && toParams.name) {
                    vName = newState + ' - ' + toParams.name;
                } else {
                    vName = newState;

                }
                appService.uvTrace(vName, function(d) {

                });
                $rootScope.$broadcast('$switchTab', -1);
                vm.tabs = {};
                var moduleName = toParams.name ? toParams.name : next.name;
                if (currentMenu) {
                    $scope.breadcrumbs = [currentMenu];
                    if (currentMenu.state != next.name) {
                        $scope.breadcrumbs.push({
                            name: moduleName
                        });

                    }
                } else {

                    $scope.breadcrumbs = [{
                        'state': 'empty',
                        'name': moduleName
                    }];
                }

            });


            // "vm.creation" is avaible by directive option "bindToController: true"
        }
    }

})();
