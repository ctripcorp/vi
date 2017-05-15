(function() {
    'use strict';

    //var apiHost = 'http://localhost:8080/vi-example-1.0/vi/api/component/';
    angular
        .module('viModule')
        .factory('customComponentStatus', function($http, ApiService) {

            var apiHost = ApiService.getEndpoint() + '/component/';
            return {
                getCustom: function(id) {
                    return $http.get(apiHost + id + "/custom");
                }
            };

        });
})();
