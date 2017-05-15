(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('LogController', LogController);


    /** @ngInject */
    function LogController($scope, logInfo, SiteCache) {
        var vm = this;
	vm.download=function(name){
		logInfo.download(name);
	};
        logInfo.getAll(function(data) {

            vm.rawLogs = data.map(function(item) {

                return {
                    name: item.name,
		    modifiedTime:new Date(item.modifiedTime),
		    size:item.size,
                    href: 'log/detail/' + item.name + '/'+item.size
                };
            });


        });

    }
})();
