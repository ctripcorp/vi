(function() {
    'use strict';

    angular
        .module('viModule')
        .controller('TraceController',

            function($element, $scope, serviceInfo, $stateParams) {
                var vm = this;
                serviceInfo.getComponentInfo('vi.ignitestatus').then(function(data) {
                    vm.data = data;
                    //Uninitiated,Running,Success,Failure

                    vm.statusClass = 'status-running';
                    vm.formatMsg = function(m) {
                        return m.replace(/\r\n/g, "<br>");
                    }
                    vm.colorMsg = function(m) {
                        if (m.indexOf('[ERROR]') >= 0) {
                            return 'status-failed';
                        } else if (m.indexOf('[WARN]') >= 0) {
                            return 'status-warn';
                        }

                    };
                    switch (data['status']) {
                        case 'Success':
                            vm.statusClass = 'status-normal';
                            break;
                        case 'Failure':
                            vm.statusClass = 'status-failed';
                            break;
                    }

                    //drawSteps(data.infos, data.currentPluginIndex, data.status);
                    nv.addGraph(function() {
                        var chart = nv.models.stepsChart();

			chart.currentIndex(data.currentPluginIndex).currentStatus(data.status);
                        d3.select('#test-chart')
                            .datum(data.infos)
                            .call(chart);

                        nv.utils.windowResize(chart.update);
                        return chart;
                    });

                });

                var viewerWidth = $element[0].scrollWidth - 30;

                var container = d3.select('svg').append('g')
                    .attr('transform', 'translate(20,30)');

                container.append('line')
                    .style("stroke", "#ccc") // colour the line
                    .attr("x1", 0) // x position of the first end of the line
                    .attr("y1", 0) // y position of the first end of the line
                    .attr("x2", viewerWidth - 40) // x position of the second end of the line
                    .attr("y2", 0);

                function drawSteps(infos, currentStep, status) {
                    var datum = infos;
                    datum.splice(0, 0, {
                        id: 'start'
                    });
                    currentStep += 1;
                    datum.push({
                        id: 'end'
                    });
                    var stepCount = datum.length;
                    var stepWidth = (viewerWidth) / (stepCount - 1);
                    var steps = container.selectAll('g')
                        .data(datum)
                        .enter()
                        .append('g')
                        .attr('class', 'step')
                        .attr('transform', function(d, i) {

                            var left = i * stepWidth;
                            if (i == stepCount - 1) {
                                left = viewerWidth - 40;
                            }
                            return 'translate(' + left + ',0)';
                        });

                    steps.append('circle')
                        .attr('r', 8)
                        .style('stroke', '#ddd')
                        .style('stroke-width', '2px')
                        .style('fill', function(d, i) {

                            var currentColor = 'lightgreen';
                            if (status == 'Success') {
                                return 'green';
                            } else if (status == 'Failure') {

                                currentColor = 'red';
                            }
                            if (i > currentStep) {
                                return '#eee';
                            } else if (i == currentStep) {

                                return currentColor;
                            } else {

                                return 'green';
                            }
                        });
                    steps.append('text').style('text-anchor', 'middle')
                        .attr('y', function(d, i) {

                            return (i % 2 == 0 || stepCount < 10) ? 25 : 35;
                        })
                        .text(function(d) {
                            return d.id;
                        });
                }
            });
})();
