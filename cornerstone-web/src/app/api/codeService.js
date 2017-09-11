(function() {
    'use strict';

    angular
        .module('viModule')
        .factory('codeService', codeService);

    /** @ngInject */
    function codeService($log, $http, ApiService, $cookies, $location, $confirm, $window, toastr, $rootScope) {
        var apiUrl = 'code/';
        var gitApiUrl; 
        var commitId; 
        var prj_path; 
        var gitFullPath;



        var initPromise = ApiService.doGetFunc(apiUrl + 'gitinfo', function(data) {
            gitApiUrl = data.url;
            commitId = data.commitId;
            prj_path = $window.encodeURIComponent(data.prjPath);
        });

        var getToken = function() {

            var privateToken = $cookies.get('gitPrivateToken');
            if (!privateToken || privateToken.length < 5) {
                $confirm({
                        text: 'You must set git private token first,',
                        title: 'No git Token Warning',
                        ok: 'Go Settings'
                    })
                    .then(function() {
                        $rootScope.$broadcast('$newViView', 'Settings', {}, 'Settings');
                    });
                return '';

            }
            return privateToken || '';
        };
        var service = {
            apiUrl: apiUrl,
            getGitFullPath: function() {
                return gitFullPath;
            },
            getGitlabPrjIdByName: function(func) {
                var privateToken = getToken();
                if (!privateToken || privateToken.length < 5) {
                    return;

                }
                return $http.get(gitApiUrl + prj_path, {
                    headers: {
                        'PRIVATE-TOKEN': privateToken
                    }
                }).then(function(rtn) {
                    gitFullPath = rtn.data.path_with_namespace;
                    func(rtn.data.id, rtn.data.path_with_namespace);
                }).catch(function(error) {
                    if (error.status === 401) {
                        $confirm({
                                text: 'Wrong Gitlab private token! You must set the right Gitlab private token!',
                                title: 'Wrong Gitlab Token Warning',
                                ok: 'Go Settings'
                            })
                            .then(function() {
                                $rootScope.$broadcast('$newViView', 'Settings', {}, 'Settings');
                            });
                    } else {
                        toastr.error(error.data.message);
                    }
                });
            },
            listFiles: function(path, func) {
                var privateToken = getToken();
                if (privateToken.length < 5) {
                    return;

                }
                var index = path.indexOf('/');
                var prjId = path.substr(0, index);
                var subPath = path.substr(index + 1);

                return $http.get(gitApiUrl + prjId + '/repository/tree', {
                    params: {
                        'ref_name': commitId,
                        'path': subPath
                    },
                    headers: {
                        'PRIVATE-TOKEN': privateToken
                    }
                }).then(function(rtn) {

                    func(rtn, path);
                });
            },
            getContent: function(path, func) {
                var index = path.indexOf('/');
                var prjId = path.substr(0, index);
                var subPath = path.substr(index + 1);
                if ((/.*\.jar$/i).test(prjId)) {
                    return ApiService.doGet(apiUrl + 'getSourceCode', {
                        'jarName': prjId,
                        'path': subPath
                    }).then(func);
                }
                var privateToken = getToken();
                if (privateToken.length < 5) {
                    return;

                }

                return $http.get(gitApiUrl + prjId + '/repository/files', {
                    params: {
                        'ref': commitId,
                        'file_path': subPath
                    },
                    headers: {
                        'PRIVATE-TOKEN': privateToken
                    }
                }).then(func);
            },
            registerBreakpoint: function(params, func) {
                return ApiService.doGet(apiUrl + 'registerbreakpoint',params).then(func);
            },
            getCapturedFrame: function(traceId, func) {

                return ApiService.doGet(apiUrl + 'getcapturedframe', {
                    'traceId': traceId
                }).then(function(data) {
                    func(data);
                });

            },
            enableDebug: function(func) {

                return ApiService.doGetFunc(apiUrl + 'enabledebug', function(data) {
                    func(data);
                });

            },
            getCodePath: function(ns, name, func) {
                return ApiService.doGet(apiUrl + 'getCodePath', {
                    'ns': ns,
                    'name': name
                }).then(function(data) {
                    func(data);
                });

            },
            getTraceKey: function(func) {
                return ApiService.doGetFunc(apiUrl + 'getTraceKey', func);

            },
            setTraceKey: function(data, func) {
                return ApiService.doPost(data, apiUrl + 'setTraceKey').then(func);

            },
            getLineVars: function(data, func) {
                data.className = data.className.replace(/\.java$/, '');
                return ApiService.doGet(apiUrl + 'getLineVars', data).then(func);

            },
            getClassFields: function(data, func) {
                data.className = data.className.replace(/\.java$/, '');
                return ApiService.doGet(apiUrl + 'getClassFields', data).then(func);

            },
            stopDebug: function(id, func) {
                return ApiService.doPost({
                    'id': id
                }, apiUrl + 'stopDebug').then(func);

            },
            isDefaultDebugger: function(func) {
                return ApiService.doGetFunc(apiUrl + 'isDefaultDebugger', func);

            },
            promise: initPromise
        };
        service.viewCode = function(ns, name) {
            service.getCodePath(ns, name.split('.')[0], function(data) {
                var tmp = name.split(':');
                if ((/^git\|/).test(data)) {

                    service.getGitlabPrjIdByName(function(prjId, prjPath) {

                        var path = prjPath.replace(/\//g, '$@') + '|' + prjId + '|' + data.substr(4).replace(/\//g, '|') + '|' + ns.replace(/\./g, '|') + '|' + tmp[0];
                        $rootScope.$broadcast('$newViView', 'Code-Content', {
                            'path': path,
                            'line': tmp[1]||''
                        }, name);
                    });

                } else {
                    var path = data + '|' + tmp[0];
                    $rootScope.$broadcast('$newViView', 'Code-Content', {
                        'path': path,
                        'line': tmp[1]||''
                    }, name);
                }
            });

        };

        return service;
    }
})();
