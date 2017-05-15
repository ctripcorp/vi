
(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('cacheService', cacheService);

    /** @ngInject */
    function cacheService($log, $http, ApiService) {
        var apiUrl = 'cache/';

        var service = {
            apiUrl: apiUrl,
            getStatus: function(type,func) {
                return ApiService.doGet(apiUrl + 'status/'+type).then(func);
            },
            getTypes: function(func) {
                return ApiService.doGet(apiUrl + 'types').then(func);
            },
            refresh: function(data, func) {
                return ApiService.doPost(data, apiUrl + 'refresh').then(func);
            }

        };

        return service;
    }
})();
