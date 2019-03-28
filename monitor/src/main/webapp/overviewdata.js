/*
 Copyright (C) 2019 Peter Withers
 
 @since 28 March 2019 21:02 PM (creation date)
 @author @author : Peter Withers <peter-gthb@bambooradical.com>
 */

$("<canvas id=\"overviewContainer\" width=\"800\" height=\"400\"></canvas>").appendTo("body");
var overviewContainer = $("#overviewContainer");
var overviewChart = new Chart(overviewContainer, {
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
    var labels = [];
    var labelsDone = false;
    $.each(locationData, function (locationKey, channelData) {
        $.each(channelData, function (channelKey, yearMonthData) {
            var setKeyFilter = "avg";
            var dataValues = [];
            $.each(yearMonthData, function (yearMonthKey, setOfData) {
                $.each(setOfData, function (setKey, daysOfData) {
                    if (setKey === setKeyFilter) {
                        $.each(daysOfData, function (index, daysValue) {
                            if (!labelsDone) {
                                var yearMonthParts = yearMonthKey.split("-");
                                labels.push(new Date(yearMonthParts[0], parseInt(yearMonthParts[1]) - 1, index));
                                dataValues.push(daysValue);
                            }
                        });
                    }
                });
            });
            if (!labelsDone) {
                overviewChart.data.labels = labels;
                console.log(labels);
                console.log(dataValues);
                overviewChart.data.datasets.push({
                    label: locationKey + " " + channelKey + " " + setKeyFilter,
                    data: dataValues,
                    backgroundColor: "rgba(255,99,132,0.2)",
                    borderColor: "rgba(255,99,132,1)"
                });
            }
            labelsDone = true;
        });
    });
});