function($scope, serviceInfo, $stateParams, toastr, $window, $element, $timeout, $confirm) {

    var vm = {};
    $scope.vm = vm;

    vm.maxHeight = ($window.innerHeight - 190) + 'px';

    var resizeFun = function() {
        $scope.$apply(function() {
            vm.maxHeight = ($window.innerHeight - 190) + 'px';
        });
    };
    serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
        vm.data = data;
    });
    angular.element($window).on('resize', resizeFun);
    $scope.$on('$destroy', function() {
        angular.element($window).off('resize', resizeFun);
    });


    vm.refresh = function() {
        serviceInfo.getComponentInfo($stateParams.id).then(function(data) {
            vm.data = data;
        });

    };

    vm.recoverClass = function() {

        var cfm = $confirm({
            text: 'Are you sure, you wanna restore this class[' + vm.currentClass + ']?',
            ok: 'Yes',
            cancel: 'No'
        }).then(function() {

            serviceInfo.doComponentMethod($stateParams.id, 'restoreClass', {
                'req': {
                    'className': vm.currentClass
                }
            }).then(function(d) {
                if (d instanceof VIException) {
                    toastr.error('Message: restore [' + vm.currentClass + '] to origin failed! because ' + d.Message + '\nType:' + d.Type);
                    return;
                }

		toastr.info('restore ['+vm.currentClass+'] success!');


            });


        });
    };

    vm.getIcon = function(t) {
        switch (t) {
            case 'metrics':
                return 'fa-tachometer';
            case 'debug':
                return 'fa-bug';

        }

    };

    vm.currentClass = '';

    vm.viewInfo = function() {
        serviceInfo.doComponentMethod($stateParams.id, 'viewDetail', {
            'req': {
                'className': vm.currentClass
            }
        }).then(function(d) {
            if (d instanceof VIException) {
                toastr.error('Message: get [' + c + '] info failed! because ' + d.Message + '\nType:' + d.Type);
                return;
            }

            vm.detailInfo = d;


        });

    };

    vm.seleClass = function(c) {

        vm.currentClass = c;

        serviceInfo.doComponentMethod($stateParams.id, 'viewModifiedASMCode', {
            'req': {
                'className': c
            }
        }).then(function(d) {
            if (d instanceof VIException) {
                toastr.error('Message: get [' + c + '] current code failed! because ' + d.Message + '\nType:' + d.Type);
                return;
            }

            var codeEle = $element[0].querySelector('pre code.current-code');
            d3.select(codeEle).html(d);

            hljs.highlightBlock(codeEle);


        });
        serviceInfo.doComponentMethod($stateParams.id, 'viewOriginASMCode', {
            'req': {
                'className': c
            }
        }).then(function(d) {

            if (d instanceof VIException) {
                toastr.error('Message: get [' + c + '] origin code failed! because ' + d.Message + '\nType:' + d.Type);
                return;
            }
            var codeEle = $element[0].querySelector('pre code.origin-code');
            d3.select(codeEle).html(d);

            hljs.highlightBlock(codeEle);


        });

    };
}
