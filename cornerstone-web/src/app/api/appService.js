
(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('appService',appService);

    /** @ngInject */
    function appService($log, $http, ApiService) {
        var apiUrl = 'app/';

        var service = {
            apiUrl: apiUrl,
            markDown: function(func) {
                return ApiService.doPost({},apiUrl + 'markdown').then(func);
            },
            markUp: function(func) {
                return ApiService.doPost({},apiUrl + 'markup').then(func);
            },
            enableStatusSource: function(func) {
                return ApiService.doPost({},apiUrl + 'enablestatussource').then(func);
            },
            disableStatusSource: function(func) {
                return ApiService.doPost({},apiUrl + 'disablestatussource').then(func);
            },
            uvTrace: function(name,func) {
                return ApiService.doPost({'name':name},apiUrl + 'uvTrace').then(func);
            }
        };

        return service;
    }
})();
