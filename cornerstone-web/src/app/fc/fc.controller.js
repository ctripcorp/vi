(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('FCController', FCController);

    function FcModalController($scope, fcService, $uibModalInstance, modifiedItems) {
            $scope.fcs = modifiedItems.items;
            $scope.ok = function() {

                var newValues = {};
                for (var i = 0; i < $scope.fcs.length; i++) {
                    var fc = $scope.fcs[i];
                    newValues[fc.key] = fc.value;
                }

                fcService.update(newValues, function(data) {
                    $uibModalInstance.close(data);
                });


            };

            $scope.cancel = function() {
                $uibModalInstance.dismiss('cancel');
            };

        }
        /** @ngInject */

    function FCController($scope, $uibModal, fcService, Permission, toastr) {
        var vm = this;

        vm.hasAuthority = true;

        vm.fcCollection = [];

        fcService.getAll(function(data, permission) {
            if (((permission & Permission.ALL) != Permission.ALL) &&
                ((permission & Permission.EDIT) != Permission.EDIT))
                vm.hasAuthority = false;

            for (var n in data) {
                var value = data[n][0];
                var remark = data[n][1];
                vm.fcCollection.push({
                    key: n,
                    value: value,
                    old: value,
                    remark: remark

                });
            }
        });

        vm.displayCollection = [];

        vm.fcNames = ['all', 'appsettings', 'centralfc'];
        vm.selectedFC = 'all';

        vm.fcFilter = function(m) {

            if (vm.selectedFC == 'all') return true;
            return m.belongFC == vm.selectedFC;
        };
        vm.openModal = function() {
            var modifiedItems = vm.fcCollection.filter(function(item) {
                return item.old != item.value;
            });
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'fcModal.html',
                controller: FcModalController,
                size: 'lg',
                resolve: {
                    modifiedItems: {
                        items: modifiedItems
                    }
                }
            });

            modalInstance.result.then(function(result) {

                if (result instanceof VIException) {

                    toastr.error(result.Message, 'submit failed!');
                    return false;
                }
                toastr.success('submit success!');
                for (var i = 0; i < vm.fcCollection.length; i++) {
                    var item = vm.fcCollection[i];
                    item.old = item.value;

                }


            }, function() {});
        };

        $scope.$on('$stateChangeStart', function(e) {
            var finded = vm.fcCollection.findIndex(function(item) {
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
