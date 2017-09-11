(function() {
    'use strict';
    angular
        .module('viModule')
        .controller('JvmSamplerController',
            function(analyzerService, $compile, $scope, $timeout) {

                var vm = this;
                var REFRESHINTERVAL = 1000;
                var timer;

                var maxTimes = 3;

                function updateData() {
                    analyzerService.getHeapHisto(function(data) {

                        if ((--maxTimes > 0) && (data === null || data.length < 10)) {
                            timer = $timeout(updateData, REFRESHINTERVAL);
                        } else {

                            var totalSize = 0,
                                totalCount = 0;
                            vm.rawList = data.map(function(m) {

                                totalSize += m[2];
                                totalCount += m[1];
                                return {
                                    'class': VIUtil.formatDesc(m[0],'#'),
                                    count: m[1],
                                    bytes: m[2]
                                };
                            });

                            vm.totalSize = VIUtil.formatBytes(totalSize);
                            vm.totalCount = totalCount;
                        }

                    });
                }

                updateData();
                $scope.$on('$destroy', function(e) {
                    $timeout.cancel(timer);
                });

            });
})();
