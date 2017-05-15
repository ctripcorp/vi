/* global malarkey:false, moment:false */
(function() {
    'use strict';

    angular
        .module('viModule')
	.constant('Permission',{
		ALL:1,
		READ:2,
		EDIT:4
	});

})();
