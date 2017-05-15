(function() {
    'use strict';
    angular
        .module('viModule')
        .controller('JvmSamplerController',
            function(analyzerService, $compile, $scope, $timeout) {

                var vm = this;
                var REFRESHINTERVAL = 1000;
                var timer;
                var fdDic = {
                    'B': 'byte',
                    'C': 'char',
                    'D': 'double',
                    'F': 'float',
                    'I': 'int',
                    'J': 'long',
                    'S': 'short',
                    'Z': 'boolean'
                };

                function formatClassName(name) {
                    var cname;


                    if (name[0] == '#') {

                        var n = 1;
                        for (var i = 1; i < name.length; i++) {

                            if (name[i] == '#') {
                                n++;
                            } else {
                                break;
                            }

                        }
                        if (name.length == n + 1) {
                            return fdDic[name[n]] + '[]'.repeat(n);

                        } else if (name[n] == 'L') {
                            return name.substr(n + 1, (name.length - n - 2)) + '[ ]'.repeat(n);
                        }
                    }
                    return name;
                }

                var maxTimes = 3;

                function updateData() {
                    analyzerService.getHeapHisto(function(data) {

                        if ((--maxTimes > 0) && (data == null || data.length < 10)) {
                            timer = $timeout(updateData, REFRESHINTERVAL);
                        } else {

                            var totalSize = 0,
                                totalCount = 0;
                            vm.rawList = data.map(function(m) {

                                totalSize += m[2];
                                totalCount += m[1];
                                return {
                                    'class': formatClassName(m[0]),
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
