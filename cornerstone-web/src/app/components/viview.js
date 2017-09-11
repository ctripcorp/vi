(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('viView', function($compile) {

            return {
                restrict: 'E',
                scope: {
                    controller: '@',
                    url: '@',
		    params:'@'
                },
                link: function(scope, element, attr) {

                    var compiledTemplate = $compile('<div params="'+attr.params+'" ng-controller="' + attr.controller + '">' +
                        '<div ng-include="\'' + attr.url + '\'" />' +
                        '</div>')(scope);
                    element.html('').append(compiledTemplate);
		    scope.$emit('$viewContentLoaded');


                }
            };

        });
})();
