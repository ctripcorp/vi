(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('elastic', [
            '$timeout',
            function($timeout) {
                return {
                    require: 'ngModel',
                    restrict: 'A',
                    link: function($scope, element, ngModel) {
                        $scope.initialHeight = $scope.initialHeight || element[0].style.height;
                        var resize = function() {
                            element[0].style.height = $scope.initialHeight;
                            element[0].style.height = "" + element[0].scrollHeight + "px";
                        };
                        $scope.$watch(ngModel.ngModel,
                            function() {
				    resize();
                            });
                    }
                };
            }
        ]);
})();
