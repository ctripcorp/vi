(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('CodeController',
            function($element, $compile, $scope, $timeout, codeService, $stateParams, $q, analyzerService, $location, $window, toastr, ScrollbarService) {
                var vm = this;
                var isJar = false;
                var currentPath = $stateParams.path;
                vm.menuMaxHeight = ($window.innerHeight - 190) + 'px';
                $element.css('minHeight', ($window.innerHeight - 100) + 'px');


                var resizeFun = function() {
                    $scope.$apply(function() {
                        vm.menuMaxHeight = ($window.innerHeight - 190) + 'px';
                        $element.css('minHeight', ($window.innerHeight - 100) + 'px');
                    });
                };
                angular.element($window).on('resize', resizeFun);

                $scope.$on('$destroy', function(e) {
                    angular.element($window).off('resize', resizeFun);
                });

                vm.backToRoot = function() {

                    if (vm.isJar(vm.currentSource)) {
                        vm.pathParts = [];


                        vm.displayList = sourceList.filter(function(x) {
                            return x.parent == '/';
                        });

                    }

                };
                vm.onClassSearchKeyDown = function(event) {
                    if (!vm.isOpen && (event.keyCode == 38 || event.keyCode == 40)) {
                        vm.isOpen = true;
                    }
                    if (vm.isOpen) {
                        var listLen = vm.classList.length;
                        switch (event.keyCode) {

                            case 13: //enter
                                vm.isOpen = false;
                                vm.viewClass(vm.classList[vm.selectedIndex]);
                                break;
                            case 38: //up

                                if (vm.selectedIndex) {

                                    vm.selectedIndex--;

                                    if (vm.selectedIndex <= 0)
                                        vm.selectedIndex = 0;

                                } else {
                                    vm.selectedIndex = listLen - 1;
                                }
                                break;
                            case 40: //down
                                if (vm.selectedIndex >= 0) {

                                    vm.selectedIndex++;

                                    if (vm.selectedIndex >= listLen)
                                        vm.selectedIndex = 0;

                                } else {
                                    vm.selectedIndex = 0;
                                }
                                break;

                        }
                    }
                };

                vm.sources = [];
                analyzerService.getJars(function(data) {
                    if (vm.sources.length > 0) {
                        var tmp = vm.sources[0];
                        vm.sources = data;
                        vm.sources.unshift(tmp);

                    } else {
                        vm.sources = data;
                        var fullPath = codeService.getGitFullPath();
                        if (fullPath) {
                            vm.sources.unshift(fullPath);
                        }
                    }

                });
                vm.isJar = function(name) {

                    return /.*\.jar$/i.test(name);
                };
                var scrollbar;
                ScrollbarService.getInstance('jar-list-scrollbar').then(function(d) {

                    scrollbar = d;
                });

                vm.refreshScroll = function() {

                    if (scrollbar && scrollbar.scrollTo) {
                        scrollbar.update();
                        scrollbar.scrollTo(0, 0);
                    }
                };


                vm.viewFolder = function(index) {
                    if (isJar) {
                        vm.pathParts = vm.pathParts.slice(0, index + 1);
                        var folderPath = vm.pathParts.join('/') + '/';
                        $location.search({
                            'fullName': folderPath
                        });
                        vm.displayList = sourceList.filter(function(x) {
                            return x.parent == folderPath;
                        });
                    } else {
                        $location.path("/code/folder/" + vm.getUrl(index));
                    }

                };
                vm.viewDetail = function(item) {

                    if (isJar) {
                        if (item.folder) {
                            $location.search({
                                'fullName': item.fullName
                            });
                            vm.displayList = sourceList.filter(function(x) {
                                return x.parent == item.fullName;
                            });
                            vm.pathParts = item.fullName.substr(0, item.fullName.length - 1).split('/');
                        } else {

                            $location.path('/code/content/jar|' + vm.currentSource + '|' + item.fullName.replace(/\//g, '|'));
                        }
                    } else {

                        $location.path(item.href);
                    }
                };

                vm.nameForUrl = function(name) {

                    return vm.isJar(name) ? 'jar|' + name : (name ? name.replace(/\//g, '$@') : '');

                };

                vm.viewClass = function(n) {

                    if (n) {
                        var index = n.lastIndexOf('/');
                        var ns = n.substr(0, index);
                        var name = n.substr(index + 1) + '.java';

                        codeService.viewCode(ns, name);
                    }
                };

                $scope.$on('$gitTokenChange', function() {
                    bindData();
                });


                var bindData = function(name) {
                    var pathParts = currentPath.split('|');
                    var currentParent = '/';
                    if (pathParts.length > 1 && pathParts[0] == 'jar') {
                        isJar = true;
                        name = pathParts[1];
                        vm.pathParts = pathParts.slice(2, pathParts.length);
                        currentParent = vm.pathParts.join('/') + '/';
                        vm.currentSource = name;
                    } else {
                        isJar = vm.isJar(name);
                    }

                    analyzerService.listClasses(isJar ? vm.currentSource : 'classes', function(d) {

                        vm.rawClassList = d.filter(function(n) {

                            return n.indexOf('$') === -1;
                        });
                    });

                    if (!isJar) {
                        if (pathParts.length < 2) {

                            codeService.getGitlabPrjIdByName(function(prjId, fullPath) {
                                vm.currentSource = fullPath;
                                if (vm.sources[0] != fullPath) {
                                    vm.sources.unshift(fullPath);
                                }
                                vm.prjId = prjId;
                                codeService.listFiles(prjId + '/', function(rtn) {
                                    if (rtn.data && rtn.data instanceof Array) {
                                        vm.list = rtn.data.map(function(x) {
                                            var isFolder = x.type == 'tree';
                                            return {
                                                'name': x.name,
                                                'folder': isFolder,
                                                'href': '/code/' + (isFolder ? 'folder' : 'content') + '/' + vm.nameForUrl(fullPath) + '|' + prjId + '|' + x.name
                                            };
                                        });

                                    }

                                });
                            });

                        } else {
                            vm.rawPrjPath = pathParts.shift();
                            vm.currentSource = vm.rawPrjPath.replace(/\$@/g, '/');
                            var folderPath = pathParts.join('/');
                            vm.prjId = pathParts.shift();
                            vm.pathParts = pathParts;

                            var getDisplayPath = function(item, path, deferred) {

                                var isFolder = item.type == 'tree';

                                if (!isFolder) {
                                    deferred.resolve(item.name);
                                } else {
                                    codeService.listFiles(path, function(rtn, path) {
                                        if (rtn.data && rtn.data instanceof Array) {
                                            if (rtn.data.length == 1 && rtn.data[0].type == 'tree') {
                                                var first = rtn.data[0];

                                                getDisplayPath(first, path + '/' + first.name, deferred);

                                            } else {
                                                deferred.resolve(path.substr(folderPath.length + 1));
                                            }
                                        }
                                    });

                                }

                                return deferred.promise;

                            };


                            codeService.listFiles(folderPath, function(rtn, path) {
                                if (rtn.data && rtn.data instanceof Array) {

                                    var data = rtn.data;
                                    var defers = [];
                                    rtn.data.forEach(function(x) {
                                        var isFolder = x.type == 'tree';
                                        x.folder = isFolder;

                                        var deferred = $q.defer();
                                        deferred = getDisplayPath(x, path + '/' + x.name, deferred).then(function(name) {
                                            x.name = name;
                                            x.href = '/code/' + (isFolder ? 'folder' : 'content') + '/' + currentPath + '|' + x.name.replace(/\//g, '|');
                                        });
                                        defers.push(deferred);

                                    });

                                    $q.all(defers).then(function() {

                                        vm.list = rtn.data;
                                    });

                                }

                            });
                        }
                    } else {

                        analyzerService.getJarSourceList(name, function(data) {
                            sourceList = data.map(function(x) {
                                var folder = x[x.length - 1] == '/';
                                var parentIndex = x.lastIndexOf('/', folder ? x.length - 2 : x.length);
                                var parentN = parentIndex > 0 ? x.substr(0, parentIndex) + '/' : '/';
                                var name = parentIndex > 0 ? x.substr(parentIndex + 1) : x;
                                name = folder ? name.substr(0, name.length - 1) : name;

                                return {
                                    'name': name,
                                    'folder': folder,
                                    'fullName': x,
                                    'href': 'javascript:void(0)',
                                    'parent': parentN
                                };
                            });

                            var fullName = $location.search().fullName;
                            if (fullName) {
                                currentParent = fullName;
                                vm.pathParts = fullName.substr(0, fullName.length - 1).split('/');
                            }


                            vm.displayList = sourceList.filter(function(x) {
                                return x.parent == currentParent;
                            });

                        });
                    }
                };

                var sourceList;

                vm.changeCodeSource = function(name) {
                    /*
                    vm.currentSource = name;
                    vm.pathParts = [];
                    bindData(name);
		    */
                    $location.path('code/folder/' + vm.nameForUrl(name));
                };
                bindData();

                vm.getUrl = function(index) {
                    var tmp = vm.pathParts.slice(0, index + 1);
                    return vm.rawPrjPath + '|' + vm.prjId + '|' + tmp.join('|');

                };


            });
})();
