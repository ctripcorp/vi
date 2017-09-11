(function() {
    'use strict';

    angular
        .module('viModule')
        .filter('sumOfValue', function() {
            return function(data, key) {
                if (angular.isUndefined(data) || angular.isUndefined(key))
                    return 0;
                var sum = 0;
                angular.forEach(data, function(value) {
                    sum = sum + parseInt(value[key]);
                });
                return sum;
            };
        })
        .filter('size', function() {

            return VIUtil.formatBytes;
        });
})();
