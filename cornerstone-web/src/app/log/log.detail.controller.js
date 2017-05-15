(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('LogDetailController', LogDetailController);

    /** @ngInject */
    function LogSearchResultStatController($scope, data, $uibModalInstance) {

        data.values.sort(function(a, b) {
            return b.value - a.value;
        });
        $scope.chartData = data.values.slice(0,
            data.values.length > 10 ? 10 : data.values.length).map(
            function(v) {
                return {
                    key: v.label,
                    y: v.value
                };
            });
        $scope.chartOptions = {
            chart: {
                type: 'pieChart',
                height: 500,
                x: function(d) {
                    return d.key;
                },
                y: function(d) {
                    return d.y;
                },
                showLabels: true,
                duration: 500,
                labelThreshold: 0.01,
                labelSunbeamLayout: true,
                legend: {
                    margin: {
                        top: 5,
                        right: 35,
                        bottom: 5,
                        left: 0
                    }
                }
            }
        };
        angular.merge($scope, data);
        $scope.close = function() {
            $uibModalInstance.dismiss('cancel');
        };
    }

    function LogDetailController($scope, $location, $stateParams, logInfo, $sce, SiteCache, $element, $uibModal, toastr, $timeout) {
        var vm = this;
        var currentKeyword = '';
        var allTags = {};
        vm.seleTags = {};
        vm.tags = [];
        var logSize = $stateParams.size;
        var maxSize = Math.ceil(logSize / 1024.0 / 1024);
        var stepSizes = d3.range(1, maxSize + 1, 1);
        var currentIndex = -1;
        var eleWidth = $element[0].offsetWidth - 60;

        var slider = d3.slider().min(1).max(maxSize).tickValues([0, Math.ceil(maxSize / 2), maxSize])
            .stepValues(stepSizes).tickFormat(function(d) {
                return d + "MB";
            });
        slider.callback(function(value) {
            if (value == currentIndex)
                return;
            currentIndex = value;
            vm.loadingComplete = false;
            allTags = {};
            vm.seleTags = {};
            vm.tags = [];
            vm.showFilter = false;
            vm.isInFilter = false;
            vm.onBottom = null;
            loadData(value);
        });
        slider.value(maxSize);
        // Render the slider in the div
        var sliderWidth = maxSize * 30;
        d3.select('.partion-selector').style('width', (sliderWidth > eleWidth ? eleWidth : sliderWidth) + 'px').call(slider);
        var container = d3.select($element[0]).select('.log-detail>.log-list').on('click', function() {
            $scope.$apply(function() {
                vm.showFilter = false;
            });
        });

        var tagRegex = /(\[\s*([^\]\s]{2,80})\s*\])|(\d{4}-\d{2}-\d{2}\s\d{2}\:\d{2}\:\d{2}\.\d{3})/g;

        var loadData = function(partitionIndex) {
            vm.foundCount = 0;
            seleTagsCount = 0;
            vm.keyword = '';
            vm.currentMatches = null;
            logInfo.getDetail($stateParams.name, {
                'encoding': ($location.search().encoding || ''),
                'partitionIndex': partitionIndex
            }, function(data) {

                var isBeginArea = true;
                var areaTag = 'normal';
                var num = 0;
                vm.lineData = {};

                container.html(
                    ('\n' + data).replace(/\n([^\n]*)(?=\n)/g, function(match, p1, p2, offset) {

                        var oldLen = p1.length;
                        var isBeginTag = false;
                        var tags = [];
                        vm.lineData[num] = [];
                        vm.lineData[num].push(tags);
                        p1 = p1.replace(tagRegex, function(match, m2, m1, m3, toffset) {
                            if (!m1) {
                                vm.lineData[num].push(Date.parse(m3));
                                if (!vm.minDate || vm.minDate > vm.lineData[num][1]) {
                                    vm.minDate = vm.lineData[num][1];
                                }

                                if (!vm.maxDate || vm.maxDate < vm.lineData[num][1]) {
                                    vm.maxDate = vm.lineData[num][1];
                                }
                                return m3;
                            }
                            var rtn = '[' + m1 + ']';
                            if (toffset === 0) {
                                areaTag = m1;
                                isBeginTag = true;
                            }

                            tags.push(m1);
                            if (m1 in allTags) {
                                allTags[m1]++;
                            } else {
                                vm.seleTags[m1] = false;
                                allTags[m1] = 1;
                            }
                            return rtn;
                        });
                        var lineClass = 'normal-line';

                        if (p1.length === 0) {
                            p1 = '\n';
                        }


                        var lineHtml = '<div class="line" num="' + (num++) + '">' + p1 + '</div>';
                        var areaBeginHtml = '<div class="area ' + areaTag +
                            '"><div class="more">...</div><span class="tools"><span class="btn show-relate"><i class="fa fa-angle-double-up"></i><i class="fa fa-angle-double-down"></i></span></span>';
                        if (isBeginArea) {
                            isBeginArea = false;
                            return areaBeginHtml + lineHtml;
                        } else if (isBeginTag) {

                            return '<div class="more">...</div></div>' + areaBeginHtml + lineHtml;
                        } else {
                            return lineHtml;
                        }
                    }) + '</div>');
                vm.loadingComplete = true;
                container.selectAll('div.area>.more:first-child').on('click', function() {
                    d3.select(this.parentElement.previousElementSibling).attr('show', 'show').attr('all', 'all').select('.more:last-child').attr('hide', 'hide');
                    d3.select(this).attr('hide', 'hide');
                });

                container.selectAll('div.area>.tools').on('click', function() {
                    var parentNode = d3.select(this.parentElement);
                    if (parentNode.attr('all') == 'all') {
                        parentNode.attr('all', null);
                    } else {
                        parentNode.attr('all', 'all');
                    }
                });

                container.selectAll('div.area>.more:last-child').on('click', function() {
                    d3.select(this.parentElement.nextElementSibling).attr('show', 'show').attr('all', 'all').select('.more:first-child').attr('hide', 'hide');
                    d3.select(this).attr('hide', 'hide');
                });

                for (var n in allTags) {
                    vm.tags.push({
                        key: n,
                        count: allTags[n]
                    });
                }



            });

        };

        vm.loadingComplete = false;
        loadData(-1);

        var showAreas = {};
        var seleTagsCount = 0;

        vm.getHighlightRegex = function() {

            var hasKeyword = currentKeyword.length !== 0;
            var regexPa = [];
            if (vm.tagRegex) {
                regexPa.push(vm.tagRegex);
            }

            if (hasKeyword) {

                regexPa.push('(' + (vm.inRegexMode ? currentKeyword : RegExp.escape(currentKeyword)) + ')');
            }
            var pattern = new RegExp(regexPa.join('|'), 'ig');
            return pattern;
        };
	var currentTags = [];
        vm.filterTag = function(tag) {
            showAreas = {};
            vm.foundCount = 0;
            vm.currentMatches = null;
            var tagEnable = vm.seleTags[tag];
            var hasKeyword = currentKeyword.length !== 0;
            if (tagEnable) {
                vm.showFilter = false;
                seleTagsCount++;
            } else {
                seleTagsCount--;
            }

            currentTags = vm.tags.filter(function(x) {

                return vm.seleTags[x.key];
            });

            vm.loadingComplete = false;
            vm.isInFilter = true;
            if (seleTagsCount === 0 && !hasKeyword) {
                delete vm.tagRegex;
                vm.clearFilter();
                return;
            } else if (seleTagsCount <= 0) {

                delete vm.tagRegex;
                vm.search();
                return;
            }

            vm.tagRegex = vm.getFilterTags().map(function(x) {
                return '(' + x.key + ')';
            }).join('|');
            container.selectAll('.area').attr('all', null);
            container.selectAll(".line").each(function(d, index) {

                var $this = d3.select(this);
                this.tagCount = this.tagCount || 0;
                var hasTag = vm.lineData[index][0].some(function(x) {
                    return x == tag;
                });
                var rtn;

                if (hasTag) {
                    if (tagEnable) {
                        this.tagCount++;
                        $this.attr('show', 'show');
                    } else {
                        this.tagCount--;
                    }

                    $this.html($this.text().replace(vm.getHighlightRegex(), function(x) {
                        return '<span class="highlight">' + x + '</span>';
                    }));

                }
                rtn = this.tagCount <= 0 ? null : d3.select(this).attr('show');

                rtn = (hasKeyword && !this.containWord) ? null : rtn;

                var $parent = d3.select(this.parentElement);
                $this.attr('show', rtn);
                if (rtn == 'show') {
                    $parent.attr('show', 'show');
                } else if ($parent.select('.line[show]').size() === 0) {
                    $parent.attr('show', null);
                }

            });
            vm.loadingComplete = true;
        };



        vm.clearFilter = function() {

            vm.loadingComplete = false;
            vm.showFilter = false;
            vm.isInFilter = false;
            seleTagsCount = 0;
            vm.keyword = '';
            currentKeyword = '';
            container.selectAll('.area').attr('all', null);
            container.selectAll('.area').attr('show', null);
            container.selectAll('.line').attr('show', function() {
                this.tagCount = 0;
                delete this.containWord;
                return null;
            });
            container.selectAll('.more').attr('hide', null);
            for (var t in vm.seleTags) {
                vm.seleTags[t] = false;
            }
            vm.loadingComplete = true;
        };

        vm.getFilterTags = function() {

		return currentTags;
        };

        var searchTimer;
        vm.asyncSearch = function() {

            if (vm.inRegexMode) return;

            if (searchTimer !== null) {
                $timeout.cancel(searchTimer);
            }
            if (vm.keyword != currentKeyword) {
                searchTimer = $timeout(function() {
                    vm.search(true);
                }, 250);
            }
        };
        vm.reset = function() {
            vm.keyword = '';
            vm.foundCount = 0;
            vm.currentMatches = null;
            vm.search();
        };
        vm.upDown = function() {
            if (vm.onBottom) {
                $location.hash('vi-log-top');
            } else {
                $location.hash('vi-log-bottom');
            }

            vm.onBottom = !vm.onBottom;
        };

        vm.showSearchStat = function() {

            var stats = [];
            for (var n in vm.currentMatches) {

                stats.push({
                    'label': n,
                    'value': vm.currentMatches[n]
                });
            }
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'log-detail-stat',
                controller: LogSearchResultStatController,
                size: 'llg',
                resolve: {
                    data: {
                        values: stats,
                        title: 'Search result statistics - ' + vm.keyword
                    }
                }
            });
        };

        vm.search = function(changeEvent) {

            if (vm.inRegexMode && changeEvent) {
                return;
            }

            if (seleTagsCount === 0 && vm.keyword.length === 0) {
                vm.clearFilter();
                return;
            }

            var keyword = vm.keyword;
            try {
                if (!changeEvent && vm.inRegexMode) {
                    keyword = new RegExp(keyword, 'gi');
                } else {
                    keyword = new RegExp(RegExp.escape(keyword), 'gi');
                }
            } catch (e) {
                toastr.error(e.message, '搜索条件有误');
                return;
            }
            container.selectAll('.area').attr('all', null);

            vm.loadingComplete = false;
            vm.isInFilter = true;
            vm.foundCount = 0;
            vm.currentMatches = {};
            currentKeyword = vm.keyword;
            var hasKeyword = currentKeyword.length !== 0;
            container.selectAll('.line').each(function() {

                var $this = d3.select(this);
                var rtn = null;
                if (seleTagsCount > 0) {

                    rtn = this.tagCount > 0 ? 'show' : null;
                }

                var currentTxt = $this.text();
                if (!hasKeyword) {

                    vm.currentMatches = null;
                    delete this.containWord;
                } else if (seleTagsCount === 0 || rtn == 'show') {

                    var matches = currentTxt.match(keyword) || [];


                    if (matches.length !== 0) {
                        angular.forEach(matches, function(v) {

                            if (v in vm.currentMatches) {
                                vm.currentMatches[v]++;
                            } else {

                                vm.currentMatches[v] = 1;
                            }
                        });
                        vm.foundCount++;
                        this.containWord = keyword;
                        rtn = 'show';
                        //d3.select(this.parentElement).attr('show', 'show');
                    } else {
                        this.containWord = false;
                        rtn = null;
                    }
                }

                $this.html(currentTxt.replace(vm.getHighlightRegex(), function(x) {
                    return '<span class="highlight">' + x + '</span>';
                }));
                $this.attr('show', rtn);
                var $parent = d3.select(this.parentElement);
                if (rtn == 'show') {
                    $parent.attr('show', 'show');
                } else if ($parent.select('.line[show]').size() === 0) {
                    $parent.attr('show', null);
                }

            });

            vm.loadingComplete = true;
        };

    }
})();
