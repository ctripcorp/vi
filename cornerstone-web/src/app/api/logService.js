(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('logInfo', logInfo);

    /** @ngInject */
    function logInfo($log, $http, ApiService) {
        //var apiHost = 'http://localhost:8080/vi-example-1.0/vi/api/component';
        var apiUrl = 'log/';

        var service = {
            apiUrl: apiUrl,
            getAll: function(func) {
                return ApiService.doGet(apiUrl + 'all').then(func);
            },
            getDetail: function(name,params ,func) {
                return ApiService.doGet(apiUrl + name,params).then(func);
            },
            update: function(data, func) {
                return ApiService.doPost(data, apiUrl + 'update').then(func);
            },
            download: function(name) {
                window.open(ApiService.getEndpoint() + '/download/log/' + name, '_self');
            }
        };

        return service;
    }
})();
