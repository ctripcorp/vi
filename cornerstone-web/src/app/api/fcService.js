(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('fcService', fcService);

    /** @ngInject */
    function fcService($log, $http, ApiService) {
        var apiUrl = 'fc/';

        var service = {
            apiUrl: apiUrl,
            getAll: function(func) {
                return ApiService.doGetFunc(apiUrl+'all',func);
			
            },
            update: function(data, func) {
                return ApiService.doPost(data,apiUrl+ 'update').then(func);
            }

        };

        return service;

    }
})();
