function($scope, serviceInfo, $stateParams) {
    var vm = {};
    $scope.vm = vm;

    serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
        vm.data = data;
    });
}
