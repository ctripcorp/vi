nv.models.stepsChart = function() {
    "use strict";

    //============================================================
    // Public Variables with Default Settings
    //------------------------------------------------------------

    var margin = {
            top: 0,
            right: 0,
            bottom: 0,
            left: 0
        },
        width = null,
        height = null,
        color = nv.utils.defaultColor(),
        tooltip = nv.models.tooltip(),
        noData = null,
        dispatch = d3.dispatch('renderEnd'),
        currentIndex = 0,
        currentStatus;

    //============================================================
    // Chart function
    //------------------------------------------------------------

    var renderWatch = nv.utils.renderWatch(dispatch);

    tooltip.contentGenerator(function(d) {

        return '<div style="padding:10px"><div class"row"><div class="col-md-3" style="text-align: right;font-weight:bolder">ID:</div><div style="padding:0px" class="col-md-9">' + d.id + '</div></div>' +
            '<div class"row"><div class="col-md-3" style="text-align: right;font-weight:bolder">ClassName:</div><div class="col-md-9" style="padding:0px">' + d.name + '</div></div>' +
            '<div class"row"><div class="col-md-3" style="text-align: right;font-weight:bolder">Before:</div><div class="col-md-9" style="padding:0px">' + d.before + '&nbsp; </div></div>' +
            '<div class"row"><div class="col-md-3" style="text-align: right;font-weight:bolder">After:</div><div class="col-md-9" style="padding:0px">' + d.after + '</div></div>' +
	    '</div>';
    });

    function chart(selection) {
            renderWatch.reset();
            //renderWatch.models(gauge);

            selection.each(function(data) {
                var container = d3.select(this);

                var wraper = container.append('g')
                    .attr('class', 'nv-stepsChart')
                    .attr('transform', 'translate(30,30)');
                nv.utils.initSVG(container);

                var availableWidth = nv.utils.availableWidth(width, container, margin),
                    availableHeight = nv.utils.availableHeight(height, container, margin);

                chart.update = function() {
                    container.selectAll('*').remove();
                    container.transition().call(chart);
                };
                chart.updateStatus = function(index, status) {

                    currentIndex = index;
                    currentStatus = status;

                    container.selectAll('circle').style('fill', stepColor);
                    container.select('line.steps-progress').attr('x2', getProgressLen);
                    //container.transition().call(chart);
                };
                chart.container = this;
                // Setup containers and skeleton of chart
                wraper.append('line')
                    .style("stroke", "#ccc") // colour the line
                    .attr("x1", 0) // x position of the first end of the line
                    .attr("y1", 0) // y position of the first end of the line
                    .attr("x2", availableWidth - 60) // x position of the second end of the line
                    .attr("y2", 0);

                var stepCount = data.length;
                var stepWidth = (availableWidth) / (stepCount - 1);
                wraper.append('line')
                    .attr('class', 'steps-progress')
                    .style("stroke", '#555') // colour the line
                    .attr("x1", 0) // x position of the first end of the line
                    .attr("y1", 0) // y position of the first end of the line
                    .attr("x2", getProgressLen) // x position of the second end of the line
                    .attr("y2", 0);

                function getProgressLen() {
                    if (currentIndex == stepCount - 1) {
                        return availableWidth - 60;
                    } else {


                        return stepWidth * currentIndex;
                    }
                }
                drawSteps(data, stepCount, stepWidth);

                function stepColor(d, i) {

                    var currentColor = 'lightgreen';
                    if (currentStatus == 'Success') {
                        return 'green';
                    } else if (currentStatus == 'Failure') {

                        currentColor = 'red';
                    }
                    if (i > currentIndex) {
                        return '#eee';
                    } else if (i == currentIndex) {

                        return currentColor;
                    } else {

                        return 'green';
                    }
                }

                function drawSteps(datum, stepCount, stepWidth) {
                    var steps = wraper.selectAll('g')
                        .data(datum)
                        .enter()
                        .append('g')
                        .attr('class', 'step')
                        .attr('transform', function(d, i) {

                            var left = i * stepWidth;
                            if (i == stepCount - 1) {
                                left = availableWidth - 60;
                            }
                            return 'translate(' + left + ',0)';
                        })
                        .on('mouseover', function(d) {
                            tooltip.data({
                                id: d.id,
                                name: d.className,
                                before: d.before,
                                after: d.after,
                                series: {}
                            }).hidden(false);
                        })
                        .on('mouseout', function(d) {
                            tooltip.hidden(true);
                        });

                    steps.append('circle')
                        .attr('r', 8)
                        .style('stroke', '#ddd')
                        .style('stroke-width', '2px')
                        .style('fill', stepColor);

                    steps.append('text').style('text-anchor', 'middle')
                        .attr('y', function(d, i) {

                            return (i % 2 == 0 || stepCount < 10) ? 25 : 35;
                        })
                        .text(function(d) {
                            return d.id;
                        });
                }

            });

            renderWatch.renderEnd('steps chart immediate');
            return chart;
        }
        //============================================================
        // Expose Public Variables
        //------------------------------------------------------------

    // expose chart's sub-components
    chart.dispatch = dispatch;
    //chart.gauge = gauge;
    chart.options = nv.utils.optionsFunc.bind(chart);


    // use Object get/set functionality to map between vars and chart functions
    chart._options = Object.create({}, {
        // simple options, just get/set the necessary values
        width: {
            get: function() {
                return width;
            },
            set: function(_) {
                width = _;
            }
        },
        currentIndex: {
            get: function() {
                return currentIndex;
            },
            set: function(_) {
                currentIndex = _;
            }
        },
        currentStatus: {
            get: function() {
                return currentStatus;
            },
            set: function(_) {
                currentStatus = _;
            }
        },
        height: {
            get: function() {
                return height;
            },
            set: function(_) {
                height = _;
            }
        },
        noData: {
            get: function() {
                return noData;
            },
            set: function(_) {
                noData = _;
            }
        },
        margin: {
            get: function() {
                return margin;
            },
            set: function(_) {
                margin.top = _.top !== undefined ? _.top : margin.top;
                margin.right = _.right !== undefined ? _.right : margin.right;
                margin.bottom = _.bottom !== undefined ? _.bottom : margin.bottom;
                margin.left = _.left !== undefined ? _.left : margin.left;
            }
        }
    });

    //nv.utils.inheritOptions(chart, gauge);
    nv.utils.initOptions(chart);

    return chart;
};
