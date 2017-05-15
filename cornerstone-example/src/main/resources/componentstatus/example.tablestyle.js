 function($scope, serviceInfo, $timeout) {

          $scope.seleType='all';
           $scope.collFilter=function(m){
             if($scope.seleType=='all'){
               return true;
             }else{
                return $scope.seleType==m.type;
             }

           };
            serviceInfo.getComponentInfo('example.tablestyle').then(function(data) {
            data.types.unshift('all');
             $scope.data=data;
            });
 }
