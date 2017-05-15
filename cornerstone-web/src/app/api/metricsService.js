(function() {
    'use strict';

    //var apiHost = 'http://localhost:8080/vi-example-1.0/vi/api/component/';
    angular
        .module('viModule')
        .factory('metricsService', function($http, ApiService) {

            var apiUrl = 'metrics/';
            return {
                getNames: function(func) {
                    return ApiService.doGet(apiUrl + "names").then(func);
                },
                register: function(data, func) {
                    return ApiService.doPost(data, apiUrl + 'register').then(func);
                },
                getCurrent: function(id, func) {
                    return ApiService.doPost({
                        'id': id
                    }, apiUrl + 'current').then(func);
                }
            };

        });
})();
