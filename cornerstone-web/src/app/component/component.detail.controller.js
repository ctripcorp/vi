(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('ComponentDetailController', function($stateParams, SiteCache, serviceInfo, $scope, ApiService) {
            var id = $stateParams.id;
            var name = id;
            var vm = this;
            vm.rowCollection = [];
            vm.componentName = $stateParams.name;
            vm.hasMeta = false;
            var meta = SiteCache.get('fieldMeta');

            var bindData = function() {
                serviceInfo.getComponentInfo(id).then(function(data) {
                    var fmetas = meta.filter(function(m) {
                        return m.id.search(name.replace('.', '\\.') + '\\.[^\\.]+$') == 0;
                    });

                    for (var i = 0; i < fmetas.length; i++) {

                        var fmeta = fmetas[i];
                        var fname = fmeta.id.replace(name, '').substr(1);
                        if (fname in data) {

                            var item = {
                                'name': fmeta.name,
                                'fname': fname,
                                'value': data[fname],
                                'description': fmeta.description
                            };
                            var extendPat = /@{[^{]*}/g;
                            var extendInfo = item.description.match(extendPat);
                            if (extendInfo != null && extendInfo.length > 0) {
                                item.description = item.description.replace(extendPat, '');
                                angular.merge(item, $scope.$eval(extendInfo[0].substr(1)));
                            }

                            if (item.isLink) {
                                item.value = '<a target="_blank" href="' + ApiService.getAbsoluteUrl('/download/config/' + item.value) + '">查看</a>';
                            } else {
                                if (fmeta) {
                                    vm.hasMeta = true;
                                    switch (fmeta['type']) {
                                        case 'Bytes':
                                            item.value = VIUtil.formatBytes(item.value);
                                            break;
                                        case 'Number':
                                            item.value = VIUtil.formatNumber(item.value);
                                            break;
                                        default:
                                            break;

                                    }
                                }
                            }

                            vm.rowCollection.push(item);

                        }

                    }

                    if (vm.rowCollection.length == 0) {
                        for (var k in data) {
                            vm.rowCollection.push({
                                'name': k,
                                'value': data[k]
                            });
                        }
                    }

                });
            };

            if (meta.filter) {
                bindData();

            } else {
                $scope.$on('fieldMeta', function(src, data) {
                    meta = data;
                    bindData();

                });
            }
        });
})();
