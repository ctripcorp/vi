(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('JardepsController',
            function($element, $compile, $scope, analyzerService, $timeout) {

                var vm = this;
                vm.type = 'net';
                vm.seleNormal = vm.seleStart = vm.seleLow = vm.seleMany = true;

                vm.showRow = function(node) {

                    switch (node.type) {
                        case 'normal':
                            return vm.seleNormal;
                        case 'start':
                            return vm.seleStart;
                        case 'low':
                            return vm.seleLow;
                        case 'many':
                            return vm.seleMany;
			default:
			    return true;

                    }
                };
                vm.normalCount = 0;
                vm.lowCount = 0;
                vm.manyCount = 0;
                $scope.$watch('vm.type', function(src) {

                    if (vm.type == 'tree') {
                        svgContainer.style('display', 'none').style('cursor', '');
                    } else {

                        svgContainer.style('display', '').style('cursor', 'move');
                    
                    }
                });
                // size of the diagram
                var viewerWidth = $element[0].scrollWidth - 20;
                $element[0].style.overflow = 'hidden';
                var documentEle = document.documentElement;
                var viewerHeight = (documentEle.scrollHeight > documentEle.clientHeight ? documentEle.scrollHeight : documentEle.clientHeight) - 140;


                var color = d3.scale.category20();
		var svgContainer =d3.select($element[0]).append('svg');
                var svg = svgContainer
                    .attr('class', 'overlay')
                    .attr('width', viewerWidth)
                    .attr('height', viewerHeight)
                    .attr('pointer-events', 'all')
                    .style('cursor', 'move')
                    .call(d3.behavior.zoom().on('zoom', function() {

                        svg.attr('transform', 'translate(' + d3.event.translate + ')' + ' scale(' +
                            d3.event.scale + ')');
                    })).append('g');

                var defs = svg.append('defs');
                var nodes;

                var arrowMarker = defs.append('marker')
                    .attr('id', 'arrow')
                    .attr('class', 'arrow')
                    .attr('markerUnits', 'strokeWidth')
                    .attr('markerWidth', '39')
                    .attr('markerHeight', '39')
                    .attr('viewBox', '0 0 30 30')
                    .attr('refX', '20')
                    .attr('refY', '6')
                    .attr('orient', 'auto');

                var arrow_path = 'M2,2 L10,6 L2,10 L6,6 L2,2';

                arrowMarker.append('path')
                    .attr('d', arrow_path)
                    .attr('fill', '#60b3e0');

                analyzerService.getDeps(function(data) {

                    vm.data = data;
                    var graph = {
                        nodes: [],
                        links: []
                    };
                    for (var i = 0; i < data.length; i++) {

                        var item = data[i];
			item.fullName = item.groupId+':'+item.artifactId+':'+item.version;

                        item.targets = [];
                        /*
                                             if (item.groupId == 'nopom') {
                                                 continue;
                                             }*/
                        graph.nodes.push({
                            'name': item.artifactId + '(' + item.version + ')',
                            'group': i + 1,
                            'data': item
                        });



                    }

                    function versionToNum(v) {
                        var matchs;
                        if (v && (matchs = v.match(/\d+\.\d+\.\d+|\d+\.\d+/g)) !== null) {
                            var parts = matchs.shift().split('.');
                            var number = 0;
                            if (parts.length == 2) {
                                parts.push(0);
                            }

                            for (var i = 0; i < parts.length; i++) {
                                number += (parts[i] - 0) * Math.pow(10000, (2 - i));
                            }
                            return number;
                        }
                    }

                    function findDepIndex(data, item, source) {

                        for (var i = 0; i < data.length; i++) {

                            var seleItem = data[i].data;
                            if (seleItem.artifactId == item.artifactId && seleItem.groupId == item.groupId) {

                                seleItem.targets.push({
                                    groupId: source.data.groupId,
                                    artifactId: source.data.artifactId,
                                    scope: source.data.scope,
                                    version: source.data.version,
                                    needVersion: item.version
                                });
                                return i;
                            }
                        }

                        return -1;

                    }

                    angular.forEach(graph.nodes, function(value, key) {


                        angular.forEach(value.data.dependencies, function(d) {
                            var index = findDepIndex(graph.nodes, d, value);
                            if (index >= 0) {
                                graph.links.push({
                                    'source': key,
                                    'target': index,
                                    'value': 1
                                });
                            }

                        });

                    });


                    var force = d3.layout.force()
                        .nodes(graph.nodes)
                        .links(graph.links)
                        .size([viewerWidth, viewerHeight])
                        .linkDistance(120)
                        .charge(-340)
                        .gravity(0.03)
                        .start();

                    var drag = force.drag().on('dragstart', function(d) {

                        d3.event.sourceEvent.stopPropagation();
                    });
                    var link = svg.selectAll('.link')
                        .data(graph.links)
                        .enter().append('line')
                        .attr('class', 'link')
                        .style('stroke-width', function(d) {

                            return 1;
                        })
                        .attr('marker-end', 'url(#arrow)');

                    var node = svg.selectAll('.node')
                        .data(graph.nodes)
                        .enter()
                        .append('g')
                        .attr('class', 'node')
                        .call(drag);
                    nodes = node;
                    node.on('mouseover', function(evt) {
                            svg.style('cursor', 'pointer');
                            $scope.$apply(function() {
                                vm.seleNode = evt.data;
                            });
                            var relateNodes = [];
                            relateNodes.push(evt.index);
                            link.attr('class', function(d) {

                                if (d.target.index == evt.index) {
                                    relateNodes.push(d.source.index);
                                }
                                if (d.source.index == evt.index) {
                                    relateNodes.push(d.target.index);
                                }

                                if (d.target.index == evt.index || d.source.index == evt.index) {
                                    return 'link';
                                } else {
                                    d3.select(this).attr('marker-end', null);
                                    return 'nonsele-link';
                                }

                            });

                            node.attr('class', function(d) {

                                if (relateNodes.indexOf(d.index) >= 0) {

                                    return 'node';
                                } else {

                                    return 'nonsele-node';
                                }
                            });

                            return;
                        })
                        .on('mouseout', function() {
                            node.attr('class', 'node');
                            svg.style('cursor', 'move');
                            link.attr('class', 'link')
                                .attr('marker-end', 'url(#arrow)');
                            return;
                        });
                    node.append('circle')
                        .attr('r', 15)
                        .style('fill', function(d) {

                            var fillColor = '#ccff33';
                            if (d.index == 0) {
                                fillColor = 'orange';
                                d.data.type = 'start';
                            } else {
                                var curVersion = versionToNum(d.data.version);
                                var versions = d.data.targets.map(function(item) {
                                    return versionToNum(item.needVersion);
                                });
                                versions.push(curVersion);
                                versions = versions.filter(function(v, i, a) {
                                    return a.indexOf(v) === i && v;
                                }).sort();
                                if (versions.length > 0) {
                                    if (curVersion != versions[versions.length - 1]) {
                                        fillColor = '#d15b47';
                                    } else if (versions.length > 1) {
                                        fillColor = '#ffff00';
                                    }
                                }
                            }
                            switch (fillColor) {
                                case '#ccff33':
                                    vm.normalCount++;
                                    d.data.type = 'normal';
                                    break;
                                case '#d15b47':
                                    d.data.type = 'low';
                                    vm.lowCount++;
                                    break;
                                case '#ffff00':
                                    d.data.type = 'many';
                                    vm.manyCount++;
                                    break;

                            }
                            return fillColor;
                        });


                    node.append('text')
                        .text(function(d) {
                            return d.name;
                        });

                    force.on('tick', function() {
                        link.attr('x1', function(d) {
                                return d.source.x;
                            })
                            .attr('y1', function(d) {
                                return d.source.y;
                            })
                            .attr('x2', function(d) {
                                return d.target.x;
                            })
                            .attr('y2', function(d) {
                                return d.target.y;
                            });

                        node.attr('transform', function(d) {
                            return 'translate(' + d.x + ',' + d.y + ')';
                        });
                    });
                });

                vm.keywordChange = function() {

                    nodes.attr('keyword', function(d) {
                        if (vm.keyword.length > 0 && d.name.indexOf(vm.keyword) >= 0) {
                            return true;
                        } else {
                            return null;
                        }
                    });

                };
            });
})();
