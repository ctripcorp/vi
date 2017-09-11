(function() {
    'use strict';
    angular
        .module('viModule')
        .controller('SelfCheckController',
            function(analyzerService, $compile, $scope, $timeout, $window, toastr, codeService) {

                var vm = this;
                var REFRESHINTERVAL = 1500;

                vm.maxHeight = ($window.innerHeight - 250) + 'px';
                var resizeFun = function() {
                    $scope.$apply(function() {
                        vm.maxHeight = ($window.innerHeight - 250) + 'px';
                    });
                };
                angular.element($window).on('resize', resizeFun);
                $scope.$on('$destroy', function(e) {
                    angular.element($window).off('resize', resizeFun);
                });

                analyzerService.getAllIgnitePlugins(function(d) {
                    vm.rawlist = d.map(function(x) {
                        return {
                            'name': x
                        };
                    });

                });



                vm.viewSource = function(event) {

                    var $this = angular.element(event.currentTarget);
                    var ns = $this.attr('jns');
                    var name = $this.text();
                    codeService.viewCode(ns, name);
                };

                vm.switchArea = function(event) {
                    var ele = event.currentTarget;
                    var contentEle = d3.select(ele.nextElementSibling);
                    var icoEle = d3.select(ele).select('i');
                    var isHidden = contentEle.style('display') == 'none';
                    contentEle.style('display', isHidden ? '' : 'none');
                    icoEle.attr('class', isHidden ? 'fa fa-caret-down' : 'fa fa-caret-right');

                };


                var urlPattern = /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)/g;
                var lastTime = {};
                var formatMsg = function(m) {

                    if (m.indexOf('[ERROR]') >= 0) {
                        m = '<span class="status-failed">' + m + '</span>';
                    } else if (m.indexOf('[WARN]') >= 0) {
                        m = '<span class="status-warn">' + m + '</span>';
                    }

                    return m.replace(/\n/g, "<br>").replace(urlPattern, function(m) {
                        return '<a target="_blank" href="' + m + '">' + m + '</a>';
                    }).replace(/(\s+at\s+)(.+)\(([a-z0-9_\$]+\.java\:\d+)\)/gi, function(match, m1, m2, m3) {
                        var tmp = m2.split('.');
                        var ns;
                        if (tmp.length > 3) {
                            ns = tmp.slice(0, tmp.length - 2).join('.');
                        }

                        return m1 + m2 + '(<span jns="' + ns + '" ng-click="vm.viewSource($event)" class="link stack-m">' + m3 + '</span>)';
                    }).replace(/^#@#(.+)/, function(match, m1, m2) {

                        var timeIndex = m1.lastIndexOf('#');
                        var time = new Date(m1.substr(timeIndex + 1));
                        var title = m1.substr(0, timeIndex);
                        var metaIndex = title.lastIndexOf('@?');
                        var meta = {};
                        var metaHtml = '';
                        if (metaIndex > 0) {
                            meta = VIUtil.queryToObj(title.substr(metaIndex + 1));
                            title = title.substr(0, metaIndex);
                        }

                        for (var a in meta) {

                            if (a != ' ') {
                                metaHtml += '<span class="meta-inf">' + a + ':' + meta[a] + '</span>';
                            }
                        }

                        if ((/^Begin/).test(title)) {
                            lastTime[title.substr(7)] = time;
                            return '<div class="block" ' + ('id' in meta ? 'id="' + meta.id + '"' : '') + '><span ng-click="vm.switchArea($event)"><i class="fa fa-caret-down"></i>' + title + metaHtml + '</span><div class="inner">';
                        } else {
				var cost = VIUtil.calculateRunTime(time - lastTime[title.substr(5)]);
				delete lastTime[title.substr(5)];
                            return title + '</div> <span class="cost-time">cost: ' +  cost + '</span>' + metaHtml + '</div>';
                        }
                    });

                };


                var timer;
                var currentIndex = 0;
                vm.msgHtml = '';

                var isCheckEnd = function(str) {

                    var checkendSymbol = '#@@#@#';
                    if (str.length <= checkendSymbol.length) {
                        return [false];
                    } else {
                        var isEnd = str.substr(0, checkendSymbol.length) == checkendSymbol;
                        return [isEnd, isEnd ? str.substr(checkendSymbol.length) : ''];
                    }
                };

                var getMsgsFun = function(uid) {
                    vm.cost = VIUtil.calculateRunTime(new Date() - vm.startTime);
                    analyzerService.getSelfCheckMsgs(uid, currentIndex, function(d) {
                        if (d instanceof Array) {
                            var isLast = false;
                            if (d.length > 0) {
                                var lastItem = d[d.length - 1];
                                var tmpHtml = '';
                                var tmp = isCheckEnd(lastItem);
                                var len = d.length;
                                if (tmp[0]) {
                                    len -= 1;
                                    isLast = true;
                                    vm.status = tmp[1] == 'true' ? 'success' : 'failed';
                                }

                                for (var i = 0; i < len; i++) {

                                    tmpHtml += formatMsg(d[i]);

                                }
                                vm.msgHtml += tmpHtml;
                                currentIndex += d.length;
                            }

                            if (!isLast) {
                                timer = $timeout(function() {
                                    getMsgsFun(uid);
                                }, REFRESHINTERVAL);
                            }
                        } else if (timer) {
                            $timeout.cancel(timer);
                        }
                    });

                };

                vm.selectName = function(m) {
                    currentIndex = 0;
                    vm.msgHtml = '';
                    if (timer) {
                        $timeout.cancel(timer);
                    }

                    vm.startTime = new Date();
                    vm.pluginID = m.name;
                    vm.status = 'running';
                    analyzerService.selfCheck(m.name, function(result) {

                        if (result instanceof VIException) {
                            vm.status = 'failed';
                            vm.msgHtml = result.Message;
                            //toastr.error(result.Message, 'submit failed!');
                        }
                        getMsgsFun(result);
                    });
                };


                vm.onKeyDown = function(event) {
                    if (!vm.isOpen && (event.keyCode == 38 || event.keyCode == 40)) {
                        vm.isOpen = true;
                    }
                    if (vm.isOpen) {
                        var listLen = vm.list.length;
                        switch (event.keyCode) {

                            case 13: //enter
                                vm.isOpen = false;
                                vm.selectName(vm.list[vm.selectedIndex]);
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

            });
})();
