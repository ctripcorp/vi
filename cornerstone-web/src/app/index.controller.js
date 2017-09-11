(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('IndexController', IndexController);

    /** @ngInject */
    function IndexController($window, SiteCache, serviceInfo, $rootScope, $scope, $state) {
        var vm = this;

        vm.winWidth = ($window.innerWidth - 110) + 'px';
        vm.winHeight = ($window.innerHeight - 93) + 'px';
        vm.collapse = function(isCollapse) {
            vm.sidebarCollapse = isCollapse;
            if (isCollapse) {
                vm.winWidth = ($window.innerWidth - 55) + 'px';
            } else {
                vm.winWidth = ($window.innerWidth - 110) + 'px';
            }

        };


        var resizeFun = function() {
            $scope.$apply(function() {
                vm.winWidth = ($window.innerWidth - 110) + 'px';
                vm.winHeight = ($window.innerHeight - 93) + 'px';
            });
        };
        angular.element($window).on('resize', resizeFun);

        $scope.$on('$destroy', function(e) {
            angular.element($window).off('resize', resizeFun);
        });

        vm.views = {};

        var lastSeleView = null;
        $scope.$on('$switchTab', function(event, index) {

            if (isNaN(index)) {
                vm.showMain = false;
		if(vm.views[lastSeleView]){
                vm.views[lastSeleView].show = false;
		}
                vm.views[index].show = true;
                lastSeleView = index;

            } else {
                vm.showMain = true;
                if (index === -1) {
                    vm.views = {};
                }
            }
        });
        $scope.$on('$removeTab', function(event, index) {

		if(lastSeleView == index){
		vm.showMain = true;
		lastSeleView = 0;
		}
		delete vm.views[index];

	});


        $scope.$on('$newViView', function(event, state, data, name) {
            var stateObj = $state.get(state);
	    if(!vm.views[state + name]){

            vm.views[state + name] = {
                controller: stateObj.controller + (stateObj.controllerAs ? ' as ' + stateObj.controllerAs : ''),
                url: stateObj.templateUrl,
                params: VIUtil.objToQuery(data),
                show: true
            };
            if (lastSeleView !== null && vm.views[lastSeleView]) {
                vm.views[lastSeleView].show = false;
            }
            lastSeleView = state + name;
	    }
            $rootScope.$broadcast('$viViewAdded', state, name);
            vm.showMain = false;

        });



        vm.showMain = true;

        SiteCache.put('fieldMeta', {});

        serviceInfo.getComponentFieldMeta().then(function(data) {

            SiteCache.put('fieldMeta', data);
            $rootScope.$broadcast('fieldMeta', data);
        });
        serviceInfo.getHostInfo().then(function(data) {

            SiteCache.put('hostInfo', data);
            $rootScope.$broadcast('hostInfo.ready', data);
            //angular.element(document).find('acme-navbar').isolateScope().setHostInfo(data.HostName, data.IP);

        });
    }
})();
