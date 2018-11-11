function VIException(msg, type) {
        this.Message = msg;
        this.Type = type;
    }
    /* global malarkey:false, moment:false */
    (function() {
        'use strict';

        angular
            .module('viModule')
            .factory('ApiService', function($window, $log, $http, API_ENDPOINT, $confirm) {

                var _api = API_ENDPOINT;

                var endpoint = _api.host ? (_api.port ? (_api.host + ':' + _api.port + _api.path) : (_api.host + _api.path)) : _api.path;

                return {
                    getEndpoint: function() {
                        return endpoint;
                    },
                    setEndpoint: function(ep) {
                        _api = ep;
                        endpoint = _api.host ? (_api.port ? (_api.host + ':' + _api.port + _api.path) : (_api.host + _api.path)) : _api.path;
                    },
                    doGet: function(path, params) {
                        var path = endpoint + '/' + path;

                        return $http.get(path, {
                                'params': params
                            })
                            .then(function(response) {

                                var result = response.data;
                                if (typeof result == "string" && result.substr(0, 6) == '$@###@') {

                                    var sindex = result.lastIndexOf('@');
                                    var exp = new VIException(result.substr(6, sindex - 6), result.substr(sindex + 1));
                                    return exp;
                                }
                                return result;
                            })
                            .catch(function(error) {

                                var result = error.data || '';
                                if (typeof result == "string" && result.substr(0, 6) == '$@###@') {

                                    var sindex = result.lastIndexOf('@');
                                    var exp = new VIException(result.substr(6, sindex - 6), result.substr(sindex + 1));
                                    if (exp.Type == 'com.ctrip.framework.cs.NoPermissionException') {
                                        $confirm({
                                                'text': 'You do not have permission for this operation or login has expired!do you wanna logout and login again?'
                                            })
                                            .then(function() {
                                                location.href = 'logout';
                                            });

                                    }
                                    return exp;
                                }

                                $log.error('XHR Failed for get' + path + '.\n' + angular.toJson(error.data, true));
                            });
                    },
                    doGetFunc: function(path, func) {
                        path = endpoint + '/' + path;

                        return $http.get(path)
                            .then(function(response) {
                                return func(response.data, response.headers('vi-permission'));
                            })
                            .catch(function(error) {

                                $log.error('XHR Failed for get' + path + '.\n' + angular.toJson(error.data, true));
                            });
                    },

                    doPost: function(data, path) {
                        var path = endpoint + '/' + path;
                        return $http({
                            url: path,
                            method: 'POST',
                            data: data
                        }).then(function(resp) {
                                var result = resp.data;
                                if (typeof result == "string" && result.substr(0, 6) == '$@###@') {

                                    var sindex = result.lastIndexOf('@');
                                    return new VIException(result.substr(6, sindex - 6), result.substr(sindex + 1));
                                }
                                return result;
                            },
                            function(error) {
                                $log.error('XHR Failed for get' + path + '.\n' + angular.toJson(error.data, true));
                            });
                    },
                    getAbsoluteUrl: function(url) {
                        return this.getEndpoint() + url;
                    }
                };
            })
            .factory('SiteCache', function($cacheFactory) {

                return $cacheFactory('siteCache');

            });

    })();
