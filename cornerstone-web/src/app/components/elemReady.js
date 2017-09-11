(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('elemReady', function($parse) {

            return {
                restrict: 'A',
                link: function($scope, element, attrs) {
                    element.ready(function() {
                        $scope.$apply(function() {
                            var func = $parse(attrs.elemReady);
                            func($scope);
                        });
                    });
                }
            };

        });
})();
