
(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('analyzerService',analyzerService);

    /** @ngInject */
    function analyzerService($log, $http, ApiService) {
        var apiUrl = 'analyzer/';

        var service = {
            apiUrl: apiUrl,
            getDeps: function(func) {
                return ApiService.doGet(apiUrl + 'deps').then(func);
            },
            getHeapHisto: function(func) {
                return ApiService.doGet(apiUrl + 'heaphisto').then(func);
            }
        };

        return service;
    }
})();
