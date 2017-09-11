(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('CacheController', CacheController);

    function CacheViewController($scope, data, $uibModalInstance, cacheService, toastr) {

        $scope.searchType = 'index';
        $scope.close = function() {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.onKeyDown = function(event) {
            if (event.keyCode == 13) {
                $scope.searchByKey();
            }

        };
        $scope.$watch('searchType', function(val) {

            if (val == 'index') {
                $scope.currentPage = 1;
                $scope.selectPage(1);
            } else {
                $scope.searchKey = '';
                $scope.jsonContent = null;

            }


        });
        $scope.title = data.id;
        $scope.type = data.$type;
        $scope.numPages = data.size;
        $scope.selectPage = function(page) {
            if (isNaN(page)) {
                toastr.error("Invalid page number, page index must be in range 1 to " + data.size);
                return false;
            }

            if (page < 1 || page > data.size) {
                toastr.error("Out of range, page index must be in range 1 to " + data.size);
                $scope.currentPage = $scope.currentPage;
                return false;
            }
            $scope.currentPage = page;
            cacheService.getByIndex({
                'id': data.id,
                'typeName': data.$type,
                'index': page - 1
            }, function(result) {
                if (result instanceof VIException) {

                    if (result.Type.indexOf('CacheNotFoundException') > 0) {
			    $scope.hasIndex = false;
			    $scope.searchType = 'key';

                    } else {
                        toastr.error(result.Message, 'get cache content failed!');
                    }
                    return false;
                }
		    $scope.hasIndex = true;
                $scope.jsonContent = result;

            });
            return true;
        };
        $scope.currentPage = 1;
        $scope.selectPage(1);
        $scope.searchByKey = function() {
            cacheService.getByKey({
                'id': data.id,
                'typeName': data.$type,
                'key': $scope.searchKey
            }, function(result) {
                if (result instanceof VIException) {

                    toastr.error(result.Message, 'get cache content failed!');
                    return false;
                }
                $scope.jsonContent = result;

            });

        };

    }

    /** @ngInject */
    function CacheController($scope, $confirm, cacheService, SiteCache, toastr, $uibModal) {
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

        vm.view = function(item) {

            item.$type = vm.selectedType.value;
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'cache-view.html',
                controller: CacheViewController,
                size: 'lg',
                resolve: {
                    data: item
                }
            });

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
