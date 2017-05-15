(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('onComplete', function($timeout) {

            return {
                restrict: 'A',
                link: function(scope, element, attr) {
                    if (scope.$last === true) {
                        $timeout(function() {
                            var compfunc = scope.$eval(attr.onComplete);
			    compfunc(element,scope);
                        });
                    }
                }
            };

        });
})();
