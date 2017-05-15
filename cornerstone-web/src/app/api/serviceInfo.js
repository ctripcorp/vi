(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('serviceInfo', serviceInfo);

    /** @ngInject */
    function serviceInfo($log, $http, ApiService, $timeout) {
        //var apiHost = 'http://localhost:8080/vi-example-1.0/vi/api/component';
        var apiUrl = 'component/';

        var service = {
            apiUrl: apiUrl,
            getServiceStatus: function() {
                return ApiService.doGet(apiUrl + 'serverStatus');
            },
            getComponentMeta: function() {
                return ApiService.doGet(apiUrl + 'meta');
            },
            getComponentFieldMeta: function() {
                return ApiService.doGet(apiUrl + 'fieldmeta');
            },
            getComponentInfo: function(id) {
                return ApiService.doGet(apiUrl + id);
            },
            getHostInfo: function() {
                return ApiService.doGet(apiUrl + 'vi.hostinfo');
            },
            doComponentMethod: function(id, methodName, para) {
                return ApiService.doPost(para, apiUrl + id + '/' + methodName);
            },
            loopWithPeriod: function(currentScope, id, interval, callback, fields) {

                var timer;
                var updateFn = function() {
                    ApiService.doGet(apiUrl + id, {
                        '$fields': fields
                    }).then(function(data) {
                        if (callback(data)) {
                            timer = $timeout(updateFn, interval);
                        }
                    })
                };
                updateFn();
                currentScope.$on('$destroy', function() {
                    $timeout.cancel(timer);
                });

                return {
                    pause: function() {

                        $timeout.cancel(timer);

                    },
                    resume: function() {

                        timer = $timeout(updateFn, interval);
                    }
                };
            }
        };

        return service;
    }
})();
