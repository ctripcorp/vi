 function($scope, serviceInfo, $timeout) {

            serviceInfo.getComponentInfo('example.simplecustom').then(function(data) {
             $scope.data=data;
            });
 }
