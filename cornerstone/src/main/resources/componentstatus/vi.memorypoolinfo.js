function($element, $compile, $scope, serviceInfo, $timeout,$uibModal) {
    var vm = $scope.vm = {};
    var REFRESHINTERVAL = 2000;
    var lastUpdateTime;
    var lastData, timer;
    var isInit = false;
    vm.startDate = d3.time.format('%Y-%m-%d %H:%M:%S')(new Date());
    var monitorStartTime;
    var cacheData = [];
    var historyMax = 3600;
    vm.mp = [];
    vm.rows = [];

    var memOption = {
        chart: {
            type: 'lineChart',
            height: 200,

            xAxis: {

                tickFormat: function(d) {

                    return d3.time.format('%H:%M:%S')(new Date(lastUpdateTime - (60 - d) * REFRESHINTERVAL));
                }
            },
            yAxis: {

                tickFormat: function(d) {
                    return VIUtil.formatBytes(d);
                }
            },
            tickFormat: function(d) {
                return d;
            },
            useInteractiveGuideline: true
        }
    };

    var memData = [{
        values: [],
        key: 'committed',
        color: '#2ca02c'
    }, {
        values: [],
        key: 'used',
        color: '#ff7f0e'
    }];


    for (var i = 0; i < 60; i++) {
        memData[0].values.push({
            x: 0,
            y: 0
        });
        memData[1].values.push({
            x: 0,
            y: 0
        });
    }


    vm.showAll =function(pool,index){

        console.log(index);
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'jvmmonitor532.html',
            controller: function($scope,$uibModalInstance){
            var vm = {};
            $scope.vm = vm;
            vm.title = pool.poolName;
            vm.max = pool.max;
            vm.init = pool.init;
            var historyOption = angular.copy(pool.Options);
            historyOption.chart.height = 500;

            historyOption.chart.xAxis = {
                tickFormat: function(d) {
                    return d3.time.format('%H:%M:%S')(new Date(monitorStartTime - 0 +(d*REFRESHINTERVAL)));
                }
            };
            var historyData = angular.copy(pool.Data);

            historyData[0].values = cacheData.map(function(v,i){
                return {'x':i,'y':v[index*2]};
            });

            historyData[1].values = cacheData.map(function(v,i){
                return {'x':i,'y':v[index*2+1]};
            });

            vm.historyOption = historyOption;
            vm.historyData = historyData;
                $scope.close = function(){
                    $uibModalInstance.dismiss('cancel');
                };
            },
            size: 'lg',
        });
    };
    function updateData() {
        serviceInfo.getComponentInfo('vi.memorypoolinfo').then(function(data) {
           if(cacheData.length > historyMax){
            cacheData.shift();
            monitorStartTime -= REFRESHINTERVAL;
            }
            if (!isInit) {
                monitorStartTime = new Date();
                isInit = true;
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var g = {};
                    g.Options = angular.copy(memOption);
                    g.poolName = item.Name;
                    g.max = VIUtil.formatBytes(item.Usage[2]);
                    g.init = VIUtil.formatBytes(item.Usage[1]);
                    g.Data = angular.copy(memData);
                    /*
                    if (item.Usage[2] > 0)
                        g.Options.chart.yDomain = [0, item.Usage[2]];
                    */

                    vm.mp.push(g);
                }

                var bag = [];
                for (var i = 0; i < vm.mp.length; i++) {
                    var item = vm.mp[i];

                    bag.push(item);
                    if (bag.length == 3) {

                        vm.rows.push(bag);
                        bag = [];
                    }

                }

                if (bag.length > 0) {
                    vm.rows.push(bag);
                }


            }

            var tmpData = [];
            for (var i = 0; i < vm.mp.length; i++) {
                var g = vm.mp[i];
                var item = data[i];
                g.Data[0].values.shift();
                g.Data[1].values.shift();

                g.Data[0].values.push({
                    x: 0,
                    y: item.Usage[0]
                });
                tmpData.push(item.Usage[0]);
                tmpData.push(item.Usage[3]);
                g.Data[1].values.push({
                    x: 0,
                    y: item.Usage[3]
                });
                for (var k = 0; k < 60; k++) {

                    g.Data[0].values[k]['x'] = k;
                    g.Data[1].values[k]['x'] = k;
                }

            }

            cacheData.push(tmpData);

            if (lastData == null) {
                lastData = data;
            }
            lastUpdateTime = new Date();

            lastData = data;
            vm.nowDate = d3.time.format('%Y-%m-%d %H:%M:%S')(new Date());
            timer = $timeout(updateData, REFRESHINTERVAL);


        });
    }
    updateData();
    $scope.$on('$destroy', function(e) {
        $timeout.cancel(timer);
    });
}
