(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('ConfigController', ConfigController);

    function ConfigModalController($scope, configService, $uibModalInstance, modifiedItems,toastr) {
            $scope.configs = modifiedItems.items;
            $scope.ok = function() {

                var newValues = {};
                for (var i = 0; i < $scope.configs.length; i++) {
                    var config = $scope.configs[i];
                    newValues[config.key] = config.value;
                }

                configService.update(newValues, function(data) {
                    toastr.success('submit success!');
                    $uibModalInstance.close(true);
                });


            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('cancel');
            };

        }
        /** @ngInject */

    function ConfigController($scope, configService, $uibModal) {
        var vm = this;

        vm.configCollection = [];

        vm.configGroups = [{
            name: 'all',
            value: 'all'
        }];
        configService.getAll(function(data) {

            for (var n in data) {
                var readOnly = n == '#';
                vm.configGroups.push({
                    name: (readOnly ? '[readonly]' : n),
                    value: n
                });
                for (var k in data[n]) {
                    vm.configCollection.push({
                        key: readOnly ? k.substr(2) : k,
                        old: data[n][k],
                        value: data[n][k],
                        readOnly: readOnly,
                        belongConfig: n
                    });
                }
            }

        });
        configService.getRemarks(function(data) {
		for(var k in data){

			var items = vm.configCollection.filter(function(item){
				return item.key == k;
			});
			if(items.length>0){
				items[0].description=data[k];
			}
		}
	});

        vm.selectedConfig = 'all';

        vm.configFilter = function(m) {

            if (vm.selectedConfig == 'all') return true;
            return m.belongConfig == vm.selectedConfig;
        };

        vm.openModal = function(item) {
            var modifiedItems = vm.configCollection.filter(function(item) {
                return item.old != item.value;
            });
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'configModal.html',
                controller: ConfigModalController,
                size: 'lg',
                resolve: {
                    modifiedItems: {
                        items: modifiedItems
                    }
                }
            });

            modalInstance.result.then(function(result) {

                if (result) {
                    for (var i = 0; i < vm.configCollection.length; i++) {
                        var item = vm.configCollection[i];
                        item.old = item.value;

                    }
                }

            }, function() {});
        };

        $scope.$on('$stateChangeStart', function(e) {
            var finded = vm.configCollection.findIndex(function(item) {
                return item.old != item.value;
            });
            if (finded >= 0) {
                if (!confirm('有未保存的改动，确认离开页面吗？')) {
                    e.preventDefault();
                }
            }

        });
    }
})();
