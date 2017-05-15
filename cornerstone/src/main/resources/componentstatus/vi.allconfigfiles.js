
function($scope, serviceInfo, $stateParams,ApiService) {
    var vm = {};
    $scope.vm = vm;

    serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
    for(var i=0;i<data.allFiles.length;i++){
        var item = data.allFiles[i];
        item.href = ApiService.getEndpoint() + '/download/config/'+item.path;
    }
        vm.data = data;
    });
}
