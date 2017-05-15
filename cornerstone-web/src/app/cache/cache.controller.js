(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('CacheController', CacheController);


    /** @ngInject */
    function CacheController($scope, $confirm, cacheService, SiteCache, toastr) {
        var vm = this;

        var loadData = function() {
            if (!vm.selectedType || !vm.selectedType.value) {
                return;
            }
            cacheService.getStatus(vm.selectedType.value, function(data) {
                vm.cellCollection = [];
                vm.cols = [];
                vm.colCount = 4;

                for (var id in data) {
                    var newItem = {
                        'id': id
                    };
                    angular.extend(newItem, data[id]);
                    vm.cellCollection.push(newItem);

                }

                if (vm.cellCollection.length > 0) {
                    var item = vm.cellCollection[0];
                    var proCount = 0;
                    for (var n in item) {

                        vm.cols.push({
                            'name': n
                        });
                        proCount++;
                    }
                    var width = 90 / proCount;
                    for (var i = 0; i < vm.cols.length; i++) {

                        vm.cols[i].width = width + '%';
                    }
                }

                vm.colCount = vm.cols.length + 1;



            });

        };

        vm.typeChange = function() {

            loadData();
        };

        vm.refresh = function(item) {

            $confirm({
                    text: 'Are you sure you want to refresh "' + item.id + '" ?'
                })
                .then(function() {
                    cacheService.refresh({
                        'id': item.id,
                        'typeName': vm.selectedType.value
                    }, function(result) {
                        if (result instanceof VIException) {

                            toastr.error(result.Message, 'refresh failed!');
                            return false;
                        }
                        toastr.success('refresh success!');
                        loadData();

                    });
                });
        };


        cacheService.getTypes(function(data, permission) {

            vm.types = data.map(function(item) {
                var lastDotIndex = item.lastIndexOf('.');
                return {
                    name: item.substr(lastDotIndex + 1),
                    value: item
                };
            });
            vm.selectedType = vm.types[0];
            loadData();
        });


    }
})();
