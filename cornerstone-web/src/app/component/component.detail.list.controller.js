(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('ComponentDetailListController', function($stateParams, SiteCache, serviceInfo, toastr) {
            var id = $stateParams.id;
            var vm = this;
            vm.rowCollection = [];
            vm.colCollection = [];
            vm.componentName = $stateParams.name;
            vm.hasMeta = false;
            vm.colCount = 3;
            vm.total = {};
            vm.getTotal = function(field, type) {

                if (field in vm.total) {
                    switch (type) {
                        case 'Bytes':
                            return VIUtil.formatBytes(vm.total[field]);
                        case 'Number':
                            return VIUtil.formatNumber(vm.total[field]);
                        default:
                            break;

                    }
                }
                return '';
            };

            serviceInfo.doComponentMethod($stateParams.id, 'list').then(function(data) {

                if (data.length <= 0)
                    return;
                var meta = SiteCache.get('fieldMeta');
                var fmetas = meta.filter(function(m) {
                    return m.id.indexOf(id) === 0;
                });
                vm.colCount = fmetas.length;
                var colWidth = Math.floor(100 / fmetas.length);
                for (var i = 0; i < fmetas.length; i++) {

                    var fmeta = fmetas[i];
                    var cwith = '*';
                    if (i !== 0) {
                        cwith = colWidth + '%';

                    }
                    var formatFn = function(val) {
                        return val;
                    };

                    var name = fmeta.name;
                    var fid = fmeta.id.substr(fmeta.id.lastIndexOf('.') + 1).toLowerCase();
                    var description = fmeta.description;
                    switch (fmeta.type) {
                        case 'Bytes':
                            vm.total[fid] = 0;
			    vm.hasTotal = true;
                            formatFn = VIUtil.formatBytes;
                            break;
                        case 'Number':
			    vm.hasTotal = true;
                            vm.total[fid] = 0;
                            formatFn = VIUtil.formatNumber;
                            break;
                        default:
                            break;

                    }

                    vm.colCollection.push({
                        name: name,
                        field: fid,
                        type: fmeta.type,
                        width: cwith,
                        description: description,
                        'formatFn': formatFn
                    });
                }

                for (i = 0; i < data.length; i++) {

                    var sele = data[i];
                    for (var n in sele) {
			var lowerName =n.toLowerCase();
                        if ((/[A-Z]/).test(n)) {
                            sele[lowerName] = sele[n];
                            delete sele[n];
                        }

                        if (lowerName in vm.total) {
                            vm.total[lowerName] += sele[lowerName];
                        }
                    }
                }
                vm.rowCollection = data;

            });



        });
})();
