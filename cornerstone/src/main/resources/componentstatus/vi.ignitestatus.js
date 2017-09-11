function($element, $scope, serviceInfo, $stateParams, $timeout, $window, toastr, codeService, ScrollbarService) {
    var vm = {};
    var timer;
    var stepsChart;
    var detailScrollbar;
    $scope.vm = vm;

    vm.maxHeight = ($window.innerHeight - 300) + 'px';
    var resizeFun = function() {
        $scope.$apply(function() {
            vm.maxHeight = ($window.innerHeight - 300) + 'px';
        });
    };
    angular.element($window).on('resize', resizeFun);
    $scope.$on('$destroy', function(e) {
        angular.element($window).off('resize', resizeFun);
        if (timer !== null)
            $timeout.cancel(timer);
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

    vm.msgHtml = '';

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
        }).replace(/([\t\s]at\s+)([^\(]+)\(([a-z0-9_\$]+\.java\:\d+)\)/gi, function(match, m1, m2, m3) {
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
                lastTime[title.substr(6)] = time;
                return '<div class="block" ' + ('id' in meta ? 'id="' + meta.id + '"' : '') + '><span ng-click="vm.switchArea($event)"><i class="fa fa-caret-down"></i>' + title + metaHtml + '</span><div class="inner">';
            } else {
                return title + '</div> <span class="meta-inf">cost: ' + VIUtil.calculateRunTime(time - lastTime[title.substr(4)]) + '</span>' + metaHtml + '</div>';
            }
        });

    };

    serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
        data.cost = VIUtil.calculateRunTime(data.cost);
        vm.data = data;
        //Uninitiated,Running,Success,Failure

        vm.statusClass = 'status-running';

        var messages = vm.data.messages;

        var html = '';
        for (var i = 0; i < messages.length; i++) {
            var m = messages[i];
            html += formatMsg(m);
        }

        vm.msgHtml = html;

        switch (data.status) {
            case 'Success':
                vm.statusClass = 'status-normal';
                break;
            case 'Failure':
                vm.statusClass = 'status-failed';
                break;
        }

        //nv.addGraph(function() {
        stepsChart = nv.models.stepsChart();
        stepsChart.stepClick(function(d) {
            detailScrollbar.update();
            var ele = document.getElementById(d.id);
            detailScrollbar.scrollTo(0, ele.offsetTop);
        });

        var myScrollbar = ScrollbarService.getInstance('ignitestatusScrollbar');
        myScrollbar.then(function(x) {

            detailScrollbar = x;
        });
        stepsChart.currentIndex(data.currentPluginIndex).currentStatus(data.status);
        d3.select('svg')
            .datum(data.infos)
            .call(stepsChart);

        nv.utils.windowResize(stepsChart.update);
        //   return stepsChart;
        //});
        if (data.status == 'Running') {
            timer = $timeout(updateData, 1000);
        }

    });

    function updateData() {

        serviceInfo.doComponentMethod($stateParams.id, 'getLastStatus', {
            'req': {
                'msgIndex': vm.data.messages.length
            }
        }).then(function(data) {

            switch (data.status) {
                case 'Success':
                    vm.statusClass = 'status-normal';
                    break;
                case 'Failure':
                    vm.statusClass = 'status-failed';
                    break;
            }
            vm.data.status = data.status;
            vm.data.cost = VIUtil.calculateRunTime(data.cost);
            vm.data.currentPluginIndex = data.currentPluginIndex;

            stepsChart.updateStatus(data.currentPluginIndex, data.status);
            var html = '';
            angular.forEach(data.messages, function(value) {
                //vm.data.messages.push(value);
                var toastType = 'info';
                html += formatMsg(value);
                if (value.length > 3) {
                    if (value.indexOf('[WARN]') >= 0) {
                        toastType = 'warning';
                    } else if (value.indexOf('[ERROR]') >= 0) {
                        toastType = 'error';
                        value = value.substr(0, value.indexOf('\r\n'));
                    }
                    if ((/^#@#(.+)/).test(value)) {
                        value = value.substr(3);
                    }
                    toastr[toastType](value);
                }

            });
            vm.msgHtml += html;
            detailScrollbar.update();

            if (data.status == 'Running') {
                timer = $timeout(updateData, 1000);
            }

        });

    }


}
