
(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('enterpriseService', enterpriseService);

    /** @ngInject */
    function enterpriseService($log, $http, ApiService) {
        var apiUrl = 'enterprise/';

        var service = {
            apiUrl: apiUrl,
            getServers: function(func) {
                return ApiService.doGetFunc(apiUrl+'servers',func);
			
            },
            getHelp: function(func) {
                return ApiService.doGetFunc(apiUrl+'help',func);
			
            }

        };

        return service;

    }
})();
