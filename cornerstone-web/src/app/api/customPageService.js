(function() {
    'use strict';

    //var apiHost = 'http://localhost:8080/vi-example-1.0/vi/api/component/';
    angular
        .module('viModule')
        .factory('customPageService', function(ApiService) {

	    var apiUrl = 'enterprise/page/';
            return {
                get: function(id) {
                    return ApiService.doGet(apiUrl + id);
                },
                doMethod: function(id, methodName, para) {
                    return ApiService.doPost(para, apiUrl + id + '/' + methodName);
                }
            };

        });
})();
