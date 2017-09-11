 function($scope, serviceInfo, $timeout) {
            var vm = $scope;

            vm.getAvgTime = function(item){
            if(item.gcCount == 0){
                return 0;
            }else{
              return ((item.gcTime/item.gcCount)).toFixed(2);
            }

            };
            serviceInfo.getComponentInfo('vi.vmsummary').then(function(data) {
                var fields = ['vmName', 'vmVendor', 'jdkVersion', 'jitCompiler', 'os', 'osArch', 'availableProcessors', 'currentThreadCount',
                'gcInfos',
                    'daemonThreadCount', 'totalStartedThreadCount', 'peakThreadCount', 'loadedClassCount', 'totalLoadedClassCount', 'unloadedClassCount', 'classPath', 'libraryPath', 'vmOptions', 'bootClassPath'
                ];
                for (var i = 0; i < fields.length; i++) {
                    var f = fields[i];
                    vm[f] = data[f];
                }
                var memFields = ['commitedVirtualMemory', 'freePhysicalMemorySize', 'totalPhysicalMemorySize', 'freeSwapSpaceSize', 'totalSwapSpaceSize', 'heapUsedMemory', 'heapMaxMemory', 'heapCommitedMemory', 'nonHeapUsedMemory', 'nonHeapMaxMemory', 'nonHeapCommitedMemory'];

                for (var i = 0; i < memFields.length; i++) {
                    var f = memFields[i];
                    vm[f] = ((data[f] || 0) / 1024 / 1024).toFixed(2) + ' MB';
                }
                vm.upTime = VIUtil.calculateRunTime(data.upTime);
                vm.processCpuTime = (data.processCpuTime / Math.pow(10, 9)).toFixed(2) + 's';

            });

        }