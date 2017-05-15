(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('IndexController', IndexController);

    /** @ngInject */
    function IndexController(SiteCache, serviceInfo,$rootScope) {
        var vm = this;

        vm.collapse = function(isCollapse) {
            vm.sidebarCollapse = isCollapse;

        };

        SiteCache.put('fieldMeta', {});

        serviceInfo.getComponentFieldMeta().then(function(data) {

            SiteCache.put('fieldMeta', data);
	    $rootScope.$broadcast('fieldMeta',data);
        });
        serviceInfo.getHostInfo().then(function(data) {

            SiteCache.put('hostInfo', data);
	    $rootScope.$broadcast('hostInfo.ready',data);
            //angular.element(document).find('acme-navbar').isolateScope().setHostInfo(data.HostName, data.IP);

        });
    }
})();
