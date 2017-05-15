 function($scope, serviceInfo, $stateParams) {

            serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
            console.log(data);
            });
            serviceInfo.doComponentMethod($stateParams.id,'getHotPoints',{'req':{'name':'simple chart',maxCount:100}}).then(function(data) {
            console.log(data);
            });
 }
