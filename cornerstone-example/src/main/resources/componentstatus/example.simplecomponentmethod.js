function($scope, serviceInfo, $stateParams, toastr) {

    $scope.doNoParaMethod = function() {

        serviceInfo.doComponentMethod($stateParams.id, 'hello').then(function(data) {

            toastr.success('do no parameter method success! return ' + data);

        });
    };
    $scope.doMethod = function() {
        serviceInfo.doComponentMethod($stateParams.id, 'hello', {
            'req': {
                'name': $scope.helloMsg
            }
        }).then(function(data) {

            toastr.success('do method success! return ' + data);

        });
    };

    $scope.doErrorMethod = function() {

        serviceInfo.doComponentMethod($stateParams.id, 'doSome', {
            'req': {
                'hasError': true
            }
        }).then(function(data) {

            if (data instanceof VIException) {
                toastr.error(data.Message, 'execute failed!');
            }

        });

    };
}
