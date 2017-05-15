(function() {
    'use strict';

    angular
        .module('viModule')
        .directive('stStickyHeader', ['$window',
            function($window) {
                return {
                    require: '^?stTable',
                    link: function(scope, element, attr, ctrl) {
                        var stickyHeader = lrStickyHeader(element[0], {
                            headerHeight: attr.stStickyHeaderTop
                        });
                        scope.$on('$destroy', function() {
                            stickyHeader.clean();
                        });

                        scope.$watch(function() {
                            return ctrl.tableState();
                        }, function() {
                            $window.scrollTo(0, lrStickyHeader.treshold);
                        }, true)
                    }
                }
            }
        ]).directive('searchWatchModel', function() {
            return {
                require: '^stTable',
                scope: {
                    searchWatchModel: '='
                },
                link: function(scope, ele, attr, ctrl) {
                    var table = ctrl;

                    scope.$watch('searchWatchModel', function(val) {
                        if (val) {
                            ctrl.search(val);
                        } else {
                                ctrl.tableState().search.predicateObject = null;
                                ctrl.tableState().pagination.start = 0;
                                ctrl.pipe();
                        }
                    });

                }
            };
        });
})();
