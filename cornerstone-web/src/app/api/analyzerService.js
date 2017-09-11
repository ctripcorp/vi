(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('analyzerService', analyzerService);

    /** @ngInject */
    function analyzerService($log, $http, ApiService) {
        var apiUrl = 'analyzer/';

        var service = {
            apiUrl: apiUrl,
            getDeps: function(func) {
                return ApiService.doGet(apiUrl + 'deps').then(func);
            },
            getJars: function(func) {
                return ApiService.doGet(apiUrl + 'jars').then(func);

            },
            getAllModuleInfo: function(func) {
                return ApiService.doGet(apiUrl + 'getAllModuleInfo').then(func);

            },
            listClasses: function(loc,func) {
                return ApiService.doGet(apiUrl + 'listClasses',{'location':loc}).then(func);

            },
            addClassForMetrics: function(name,func) {
                return ApiService.doGet(apiUrl + 'addClassForMetrics',{'name':name}).then(func);
            },
            getJarSourceList: function(name,func) {
                return ApiService.doGet(apiUrl + 'listsource',{'jarname':name}).then(func);

            },
            getNeedMetricsClasses: function(func) {
                return ApiService.doGet(apiUrl + 'getNeedMetricsClasses').then(func);

            },
            removeMetricsClass: function(name,func) {
                return ApiService.doGet(apiUrl + 'removeMetricsClass',{'name':name}).then(func);

            },
            getHeapHisto: function(func) {
                return ApiService.doGet(apiUrl + 'heaphisto').then(func);
            },
            getVMSnapshot: function(func) {
                return ApiService.doGet(apiUrl + 'vmsnapshot').then(func);
            },
            getGCLogList: function(func) {
                return ApiService.doGet(apiUrl + 'getgcloglist').then(func);
            },
            parseGCLog: function(name, func) {
                return ApiService.doGet(apiUrl + 'parsegclog', {
                    'name': name
                }).then(func);
            },
            getJVMOptions: function(func) {
                return ApiService.doGet(apiUrl + 'getjvmoptions').then(func);
            },
            getAllIgnitePlugins: function(func) {
                return ApiService.doGet(apiUrl + 'allIgnitePlugins').then(func);
            },
            selfCheck: function(id,func) {
                return ApiService.doGet(apiUrl + 'selfcheck',{'id':id}).then(func);
            },
            getSelfCheckMsgs: function(uid,index,func) {
                return ApiService.doGet(apiUrl + 'getSelfCheckMsgs',{'uid':uid,'index':index}).then(func);
            }
        };

        return service;
    }
})();
