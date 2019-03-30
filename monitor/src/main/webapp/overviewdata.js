/*
 Copyright (C) 2019 Peter Withers
 
 @since 28 March 2019 21:02 PM (creation date)
 @author @author : Peter Withers <peter-gthb@bambooradical.com>
 */
var overviewChart;
var graphDataChannels = {};
$(document).ready(function () {
    $("<canvas id=\"overviewContainer\" width=\"800\" height=\"400\"></canvas>").appendTo("body");
    var overviewContainer = $("#overviewContainer");
    overviewChart = new Chart(overviewContainer, {
        type: 'line',
        data: {
            datasets: []
        },
        options: {
            bezierCurve: false,
            responsive: true,
            maintainAspectRatio: true,
            scales: {
                xAxes: [{
                        type: 'time',
                        time: {
                            displayFormats: {
                                quarter: 'YYYY MMM D H:mm:ss'
                            },
                            tooltipFormat: 'YYYY MMM D H:mm:ss'
                        }
                    }]}
        }});
    $.getJSON("/monitor/overview", function (locationData) {
        $.each(locationData, function (locationKey, channelData) {
            $.each(channelData, function (channelKey, yearMonthData) {
                var locationChannel = (locationKey + "_" + channelKey).split(" ").join("_");
                graphDataChannels[locationChannel] = {"avg": [], "min": [], "Q1": [], "Q2": [], "Q3": [], "max": []};
                $.each(yearMonthData, function (yearMonthKey, setOfData) {
                    $.each(setOfData, function (setKey, daysOfData) {
                        $.each(daysOfData, function (index, daysValue) {
                            var yearMonthParts = yearMonthKey.split("-");
                            var dateX = new Date(yearMonthParts[0], parseInt(yearMonthParts[1]) - 1, index);
                            graphDataChannels[locationChannel][setKey].push({'x': dateX, 'y': daysValue});
                        });
                    });
                });
                $("<button onclick=\"addChannel('" + locationChannel + "')\">" + locationKey + " " + channelKey + "</button>").appendTo("body");
            });
        });
    });
});
function addChannel(locationChannel) {
    overviewChart.data.datasets.push({
        label: locationChannel + " avg",
        data: graphDataChannels[locationChannel].avg,
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)",
        fill: false
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " min",
        data: graphDataChannels[locationChannel].min,
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)",
        fill: false
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " Q1",
        data: graphDataChannels[locationChannel].Q1,
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)",
        fill: +1
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " Q2",
        data: graphDataChannels[locationChannel].Q2,
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)",
        fill: -1
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " Q3",
        data: graphDataChannels[locationChannel].Q3,
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)",
        fill: -1
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " max",
        data: graphDataChannels[locationChannel].max,
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)",
        fill: false
    });
    overviewChart.update();
}