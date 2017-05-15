
(function() {
    'use strict';

    angular
        .module('viModule')
        .filter('size',function(){
	
	   return VIUtil.formatBytes;
	});
})();
