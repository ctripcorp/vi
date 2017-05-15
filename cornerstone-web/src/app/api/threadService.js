
(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('threadService', threadService);

    /** @ngInject */
    function threadService($log, $http, ApiService) {
        var apiUrl = 'threading/';

        var service = {
            apiUrl: apiUrl,
            getAll: function(func) {
                return ApiService.doGet(apiUrl + 'all').then(func);
            },
            getStats: function(func) {
                return ApiService.doGet(apiUrl + 'stats').then(func);
            },
            dump: function(params,func) {
                return ApiService.doGet(apiUrl + 'dump',params).then(func);
            },
            getDetail: function(id, params,func) {
                return ApiService.doGet(apiUrl +'detail/'+ id,params).then(func);
            },
            update: function(data, func) {
                return ApiService.doPost(data, apiUrl + 'update').then(func);
            }

        };

        return service;
    }
})();
