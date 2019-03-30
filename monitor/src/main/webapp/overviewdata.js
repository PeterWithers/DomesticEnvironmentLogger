/*
 Copyright (C) 2019 Peter Withers
 
 @since 28 March 2019 21:02 PM (creation date)
 @author @author : Peter Withers <peter-gthb@bambooradical.com>
 */
var overviewChart;
var graphDataChannels = {};
var channelColours = {
    'Temperature 2': {label: 'Temperature 2',
        backgroundColor: "rgba(179,181,198,0.2)",
        borderColor: "rgba(179,181,198,1)"},
    'Temperature 1': {label: 'Temperature 1',
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)"},
    'Temperature 3': {
        label: 'Temperature 3',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'Temperature 4': {
        label: 'Temperature 4',
        backgroundColor: "rgba(75, 92, 192, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'aquariuma0': {
        label: 'AquariumA',
        backgroundColor: "rgba(75, 92, 192, 0.0)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'aquariuma1': {
        label: 'AquariumB',
        backgroundColor: "rgba(95, 52, 192, 0.0)",
        borderColor: "rgba(95, 52, 192, 1)"},
    'rearwall top floor0': {
        label: 'rearwall0',
        backgroundColor: "rgba(200,100,200, 0.2)",
        borderColor: "rgba(200,100,200, 1)"},
    'rearwall top floor1': {
        label: 'rearwall1',
        backgroundColor: "rgba(180,100,200, 0.2)",
        borderColor: "rgba(180,100,200, 1)"},
    'rearwall top floor2': {
        label: 'rearwall2',
        backgroundColor: "rgba(160,100,200, 0.2)",
        borderColor: "rgba(160,100,200, 1)"},
    'frontwall top floor0': {
        label: 'frontwall0',
        backgroundColor: "rgba(200,200,100, 0.2)",
        borderColor: "rgba(200,200,100, 1)"},
    'frontwall top floor1': {
        label: 'frontwall1',
        backgroundColor: "rgba(180,200,100, 0.2)",
        borderColor: "rgba(180,200,100, 1)"},
    'frontwall top floor2': {
        label: 'frontwall2',
        backgroundColor: "rgba(160,200,100, 0.2)",
        borderColor: "rgba(160,200,100, 1)"}
};

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
                $("<button onclick=\"addChannel('" + locationKey + "', '" + locationChannel + "')\">" + locationKey + " " + channelKey + "</button>").appendTo("body");
            });
        });
    });
});
function addChannel(locationKey, locationChannel) {
    overviewChart.data.datasets.push({
        label: locationChannel + " avg",
        data: graphDataChannels[locationChannel].avg,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: false
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " min",
        data: graphDataChannels[locationChannel].min,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: false
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " Q1",
        data: graphDataChannels[locationChannel].Q1,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: +1
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " Q2",
        data: graphDataChannels[locationChannel].Q2,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: -1
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " Q3",
        data: graphDataChannels[locationChannel].Q3,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: -1
    });
    overviewChart.data.datasets.push({
        label: locationChannel + " max",
        data: graphDataChannels[locationChannel].max,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: false
    });
    overviewChart.update();
}