(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('ComponentController', ComponentController);

    /** @ngInject */
    function ComponentController($element, $scope, serviceInfo, SiteCache, $rootScope, $confirm, appService, toastr, $cookies, ApiService, $filter) {
        var vm = this;
        var isOwner = false;
        vm.isPause = false;

        vm.circleClass = 'fa-circle';

        vm.buildInfo = [];
        var meta = SiteCache.get('fieldMeta');
        if (meta == null || meta.filter == null) {
            serviceInfo.getComponentFieldMeta().then(function(data) {
                meta = data;
                bindAppSummary();
            });
        } else {
            bindAppSummary();
        }

        vm.changeCircle = function() {

            vm.isPause = !vm.isPause;

            if (vm.timer) {
                if (vm.isPause) {
                    vm.timer.pause();
                    vm.timerPauseTime = new Date();
                } else {
                    upTime += (new Date() - vm.timerPauseTime);
                    vm.timer.resume();
                }
            }
            vm.circleClass = vm.isPause ? 'fa-pause-circle' : 'fa-circle';

        };

        var getData = function(data, name) {

            var rtn = [];
            if (name == 'vi.hostinfo') {

                rtn.push({
                    'name': 'Host',
                    'value': data['HostName'] + '/' + data['IP']
                });
                delete data.HostName;
                delete data.IP;

            }

            var fmetas = meta.filter(function(m) {
                return m.id.search(name.replace('.', '\\.') + '\\.[^\\.]+$') == 0;
            });
            for (var i = 0; i < fmetas.length; i++) {

                var fmeta = fmetas[i];
                var fname = fmeta.id.replace(name, '').substr(1);
                if (fname in data) {

                    if (name === 'vi.hostinfo' && fname === 'serverXml' && !data[fname]) {

                        continue;
                    }

                    var item = {
                        'name': fmeta.name,
                        'fname': fname,
                        'value': data[fname],
                        'description': fmeta.description
                    };
                    var extendPat = /@{[^{]*}/g;
                    var extendInfo = item.description.match(extendPat);
                    if (extendInfo !== null && extendInfo.length > 0) {
                        item.description = item.description.replace(extendPat, '');
                        angular.merge(item, $scope.$eval(extendInfo[0].substr(1)));
                    }

                    if (item.isLink) {
                        item.html = '<a target="_blank" href="' + ApiService.getAbsoluteUrl('/download/config/' + item.value) + '">查看</a>';
                        item.value = '';
                    }

                    if (name == 'vi.hostinfo') {
                        vm.sysInfoPretreatment(item);

                    }
                    rtn.push(item);

                }

            }
            return rtn;
        };

        vm.changeStatus = function(info) {

            var operation = 'markUp';

            if (info.value == 'Initiated') {
                operation = 'markDown';
            }
            var cfm = $confirm({
                text: 'Are you sure to [' + operation + '] this Application?'
            });
            cfm.then(function() {
                appService[operation](function(result) {

                    if (result instanceof VIException) {

                        toastr.error(result.Message, 'submit failed!');
                        return false;
                    }
                    if (operation == 'markDown') {
                        info.value = 'MarkDown';
                    } else {
                        info.value = 'Initiated';
                    }

                });

            });

        };
        vm.sysInfoPretreatment = function(info) {
            var hostInfo = SiteCache.get('hostInfo');
            switch (info.fname) {
                case 'PhysicalMemoryAvaliable':
                    var val = info.value;
                    var total = hostInfo['PhysicalMemoryTotal'];
                    info.value = VIUtil.formatBytes(info.value, 'M');
                    info.cssClass = val / total < 0.3 ? 'data-warn' : 'data-normal';
                    break;
                case 'PhysicalMemoryTotal':
                    info.value = VIUtil.formatBytes(info.value, 'M');
                    break;
                case 'availableMem':
                    if (info.value > 0) {
                        var total = (hostInfo['PhysicalMemoryTotal']);
                        info.value = VIUtil.formatBytes(info.value, 'M');
                        info.cssClass = info.value / total < 0.3 ? 'data-warn' : 'data-normal';
                    } else {
                        info.value = 'N/A';
                    }
                    break;
                case 'DiskAvaliable':
                    var total = hostInfo['DiskTotal'];
                    info.value = VIUtil.formatBytes(info.value);
                    info.cssClass = info.value / total < 0.3 ? 'data-warn' : 'data-normal';
                    break;
                case 'DiskTotal':
                    info.value = VIUtil.formatBytes(info.value);
                    break;
                case 'systemCpuLoad':
                    info.cssClass = info.value > 0.5 ? 'data-warn' : 'data-normal';
                    info.value = (info.value * 100).toFixed(2) + '%';
                    break;
                case 'openFileDescriptorCount':
                    if (info.value > 0) {
                        var val = info.value;
                        var total = hostInfo['maxFileDescriptorCount'];
                        info.cssClass = val / total > 0.3 ? 'data-warn' : 'data-normal';
                    } else {
                        info.value = 'N/A';
                        return '';

                    }

                case 'maxFileDescriptorCount':
                    if (info.value < 0) {
                        info.value = 'N/A';
                    }
                    break;
            }

        };

        vm.getValHtml = function(info) {
            var rtn = '';
            switch (info.fname) {
                case 'appStatus':
                    //Uninitiated,Initiating,Initiated,InitiatedFailed,MarkDown
                    var cssClass = '';
                    switch (info.value) {
                        case 'Uninitiated':
                        case 'Initiating':
                            cssClass = 'status-running';
                            break;
                        case 'Initiated':
                            cssClass = 'status-normal';
                            break;
                        case 'InitiatedFailed':
                            cssClass = 'status-failed';
                            break;
                        case 'MarkDown':
                            cssClass = 'status-warn';
                            break;
                    }
                    var showTxt = info.value == 'Initiated' ? 'Active' : 'InActive' + '(' + info.value + ')';
                    rtn = '<a href="#/component/custom/vi.ignitestatus/ignite status" class="' + cssClass + ' mylink">' + showTxt;
                    if (cssClass == 'status-running') {
                        rtn += '<i class="fa fa-spinner fa-pulse fa-fw"></i>'
                    } else if (isOwner && (info.value == 'Initiated' || info.value == 'MarkDown')) {
                        rtn = rtn + '</a><button style="margin-left:20px" class="btn btn-primary" type="button" ng-click="vm.changeStatus(info)">' +
                            (info.value == 'Initiated' ? 'Mark down' : 'Mark up') + '</button>';
                        return rtn;
                    }
                    rtn += '</a>';
                    break;
                case 'webInfo':
                    rtn = '<a href="' + ApiService.getAbsoluteUrl('/download/config/root_web.xml') + '" target="_blank">查看</a>';
                    break;
		case 'statusSourceNames':
		    if(info.value instanceof Array){
			    info.value = info.value.join(' , ');
		    }
		    rtn = '{{info.value.length>60 && !info.showAll?info.value.substr(0,59)+"...":info.value}}<span ng-click="info.showAll = !info.showAll" class="link" ng-show="info.value.length>60">{{info.showAll?"less":"more"}}</span>';
		    break;
                default:
                    rtn = info.value;
                    break;
            }

            if (info.link && info.linkTxt) {
                rtn += '<a href="' + info.link + '" target="_blank">  ' + info.linkTxt + '</a>';
            }

            return rtn;
        };

        vm.rawComponents = null;

        $rootScope.$watch('components', function() {

            if (vm.rawComponents === null && $rootScope.components) {
                vm.rawComponents = $rootScope.components;
            }
        });


        var upTimeDS, upTime;

        function bindAppSummary() {
            serviceInfo.getComponentInfo('vi.appinfo').then(function(data) {
                if (data) {
                    upTime = data.upTime;
                    data.upTime = VIUtil.calculateRunTime(data.upTime);

                    vm.buildInfo = getData(data, 'vi.appinfo');

                    for (var i = 0; i < vm.buildInfo.length; i++) {
                        if (vm.buildInfo[i].fname == 'upTime') {
                            upTimeDS = vm.buildInfo[i];
                            break;
                        }
                    }

                    var nowUser = $cookies.get('vi-user');
                    isOwner = (nowUser.length > 2 && nowUser == data['appOwner']);
                    $rootScope.$broadcast('appinfo.ready', data);

                    if (data['appStatus'] == 'Uninitiated' || data['appStatus'] == 'Initiating') {
                        serviceInfo.loopWithPeriod($scope, 'vi.appinfo', 2500, function(data) {
                            for (var n in data) {
                                angular.forEach(vm.buildInfo, function(value) {
                                    if (value.fname == n) {
                                        value.value = data[n];
                                    }

                                });
                            }

                            return (data['appStatus'] == 'Uninitiated' || data['appStatus'] == 'Initiating');
                        }, 'appStatus,latestNews');
                    }
                }
            });

            serviceInfo.getHostInfo().then(function(data) {

                SiteCache.put('hostInfo', data);
                vm.sysInfo = getData(data, 'vi.hostinfo');
                var interval = 2500;
                vm.timer = serviceInfo.loopWithPeriod($scope, 'vi.hostinfo', interval, function(data) {
                    for (var n in data) {
                        angular.forEach(vm.sysInfo, function(value) {
                            if (value.fname == n) {
                                value.value = data[n];
                                vm.sysInfoPretreatment(value);
                            }

                        });
                    }

                    if (upTimeDS) {
                        upTime += interval;
                        upTimeDS.value = VIUtil.calculateRunTime(upTime);
                    }
                    return true;
                }, 'systemCpuLoad,availableMem,PhysicalMemoryAvaliable,DiskAvaliable,openFileDescriptorCount,cpuLoadAverages');
            });
        }
    }

})();
