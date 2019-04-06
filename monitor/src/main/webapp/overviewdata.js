/*
 Copyright (C) 2019 Peter Withers
 
 @since 28 March 2019 21:02 PM (creation date)
 @author @author : Peter Withers <peter-gthb@bambooradical.com>
 */
var overviewChart;
var graphDataChannels = {};
var channelColours = {
//    'Temperature 2': {label: 'Temperature 2',
//        backgroundColor: "rgba(179,181,198,0.2)",
//        borderColor: "rgba(179,181,198,1)"},
//    'Temperature 1': {label: 'Temperature 1',
//        backgroundColor: "rgba(255,99,132,0.2)",
//        borderColor: "rgba(255,99,132,1)"},
//    'Temperature 3': {
//        label: 'Temperature 3',
//        backgroundColor: "rgba(75, 192, 192, 0.2)",
//        borderColor: "rgba(75, 192, 192, 1)"},
//    'Temperature 4': {
//        label: 'Temperature 4',
//        backgroundColor: "rgba(75, 92, 192, 0.2)",
//        borderColor: "rgba(75, 92, 192, 1)"},
//    'aquariuma0': {
//        label: 'AquariumA',
//        backgroundColor: "rgba(75, 92, 192, 0.0)",
//        borderColor: "rgba(75, 92, 192, 1)"},
//    'aquariuma1': {
//        label: 'AquariumB',
//        backgroundColor: "rgba(95, 52, 192, 0.0)",
//        borderColor: "rgba(95, 52, 192, 1)"},
//    'rearwall top floor0': {
//        label: 'rearwall0',
//        backgroundColor: "rgba(200,100,200, 0.2)",
//        borderColor: "rgba(200,100,200, 1)"},
//    'rearwall top floor1': {
//        label: 'rearwall1',
//        backgroundColor: "rgba(180,100,200, 0.2)",
//        borderColor: "rgba(180,100,200, 1)"},
//    'rearwall top floor2': {
//        label: 'rearwall2',
//        backgroundColor: "rgba(160,100,200, 0.2)",
//        borderColor: "rgba(160,100,200, 1)"},
//    'frontwall top floor0': {
//        label: 'frontwall0',
//        backgroundColor: "rgba(200,200,100, 0.2)",
//        borderColor: "rgba(200,200,100, 1)"},
//    'frontwall top floor1': {
//        label: 'frontwall1',
//        backgroundColor: "rgba(180,200,100, 0.2)",
//        borderColor: "rgba(180,200,100, 1)"},
//    'frontwall top floor2': {
//        label: 'frontwall2',
//        backgroundColor: "rgba(160,200,100, 0.2)",
//        borderColor: "rgba(160,200,100, 1)"}
    'invalid:second top floor': {
        label: 'invalid:second top floor',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'manualtest1': {
        label: 'manualtest1',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'manualtest0': {
        label: 'manualtest0',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'rearwall top floor0': {
        label: 'rearwall top floor0',
        backgroundColor: "rgba(200,100,200, 0.2)",
        borderColor: "rgba(200,100,200, 1)"},
    'rearwall top floor2': {
        label: 'rearwall top floor2',
        backgroundColor: "rgba(160,100,200, 0.2)",
        borderColor: "rgba(160,100,200, 1)"},
    'third ground floor0': {
        label: 'third ground floor0',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'rearwall top floor1': {
        label: 'rearwall top floor1',
        backgroundColor: "rgba(180,100,200, 0.2)",
        borderColor: "rgba(180,100,200, 1)"},
    'aquarium0': {
        label: 'aquarium0',
        backgroundColor: "rgba(75, 92, 192, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'aquariuma1': {
        label: 'aquariuma1',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'aquariuma0': {
        label: 'aquariuma0',
        backgroundColor: "rgba(75, 92, 192, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'third testing messaging': {
        label: 'third testing messaging',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'second testing board': {
        label: 'second testing board',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'aquariumb10': {
        label: 'aquariumb10',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'aquariuma1-manually-entered-data': {
        label: 'aquariuma1-manually-entered-data',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'rearwall top floor': {
        label: 'rearwall top floor',
        backgroundColor: "rgba(200,100,200, 0.2)",
        borderColor: "rgba(200,100,200, 1)"},
    'second top floor': {
        label: 'second top floor',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'second top floor0': {
        label: 'second top floor0',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'frontwall top floor2': {
        label: 'frontwall top floor2',
        backgroundColor: "rgba(160,200,100, 0.2)",
        borderColor: "rgba(160,200,100, 1)"},
    'frontwall top floor1': {
        label: 'frontwall top floor1',
        backgroundColor: "rgba(180,200,100, 0.2)",
        borderColor: "rgba(180,200,100, 1)"},
    'frontwall top floor0': {
        label: 'frontwall top floor0',
        backgroundColor: "rgba(200,200,100, 0.2)",
        borderColor: "rgba(200,200,100, 1)"},
    'second top floora0': {
        label: 'second top floora0',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'aquariumb0': {
        label: 'aquariumb0',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'aquarium': {
        label: 'aquarium',
        backgroundColor: "rgba(75, 92, 192, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'deelen': {
        label: 'deelen',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'precipitationdeelen': {
        label: 'precipitationdeelen',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'testing first floor': {
        label: 'testing first floor',
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)"},
    'testing first floor0': {
        label: 'testing first floor0',
        backgroundColor: "rgba(255,99,132,0.2)",
        borderColor: "rgba(255,99,132,1)"},
    'testing board': {
        label: 'testing board',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'invalid-second top floor': {
        label: 'invalid-second top floor',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'third ground floor': {
        label: 'third ground floor',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'windspeeddeelen': {
        label: 'windspeeddeelen',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
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
//            legend: {
//                display: false,
//            },
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
    var columnLabels = [];
    $("<table id=\"buttonsTable\"/>").appendTo("body");
    $("<tr id=\"headerRow\"><td id=\"emptyCell\"/></tr>").appendTo("#buttonsTable");
    $.getJSON("/monitor/overview", function (locationData) {
        Object.keys(locationData).sort().forEach(function (locationKey) {
            var channelData = locationData[locationKey];
            var locationId = locationKey.replace(/ /g, "_");
            $("<tr id=\"" + locationId + "\"><td>" + locationKey + "</td></tr>").appendTo("#buttonsTable");
            for (var value in columnLabels) {
                $("<td id=\"" + columnLabels[value] + "_" + locationId + "\">").appendTo("#" + locationId);
            }
//            $("<div>'" + locationKey + "': {</div>").appendTo("body");
//        $("<div>label: '" + locationKey + "',</div>").appendTo("body");
//        $("<div>backgroundColor: \"rgba(123,123,123, 0.2)\",</div>").appendTo("body");
//        $("<div>borderColor: \"rgba(123,123,123, 1)\"},</div>").appendTo("body");
            $.each(channelData, function (channelKey, yearMonthData) {
                if (columnLabels.indexOf(channelKey) < 0) {
                    columnLabels.push(channelKey);
                    $("<td>" + channelKey + "</td>").appendTo("#headerRow");
                    $("<td id=\"" + channelKey + "_" + locationId + "\"/>").appendTo("#" + locationId);
                }
                var locationChannel = (locationId + "_" + channelKey).split(" ").join("_");
                graphDataChannels[locationChannel] = {"avg": [], "min": [], "Q1": [], "Q2": [], "Q3": [], "max": []};
                Object.keys(yearMonthData).sort().forEach(function (yearMonthKey) {
                    $.each(yearMonthData[yearMonthKey], function (setKey, daysOfData) {
                        $.each(daysOfData, function (index, daysValue) {
                            if (daysValue > 0) {
                                var yearMonthParts = yearMonthKey.split("-");
                                var dateX = new Date(yearMonthParts[0], parseInt(yearMonthParts[1]) - 1, index);
                                graphDataChannels[locationChannel][setKey].push({'x': dateX, 'y': daysValue});
                            }
                        });
                    });
                });
                $("<button style=\"border: " + channelColours[locationKey].borderColor + " 3px solid; background:" + channelColours[locationKey].backgroundColor + ";\" onclick=\"addChannelMin('" + locationKey + "', '" + locationChannel + "')\">min</button>").appendTo("#" + channelKey + "_" + locationId);
                $("<button style=\"border: " + channelColours[locationKey].borderColor + " 3px solid; background:" + channelColours[locationKey].backgroundColor + ";\" onclick=\"addChannelAvg('" + locationKey + "', '" + locationChannel + "')\">avg</button>").appendTo("#" + channelKey + "_" + locationId);
                $("<button style=\"border: " + channelColours[locationKey].borderColor + " 3px solid; background:" + channelColours[locationKey].backgroundColor + ";\" onclick=\"addChannelMax('" + locationKey + "', '" + locationChannel + "')\">max</button>").appendTo("#" + channelKey + "_" + locationId);
                $("<button style=\"border: " + channelColours[locationKey].borderColor + " 3px solid; background:" + channelColours[locationKey].backgroundColor + ";\" onclick=\"addChannelQ123('" + locationKey + "', '" + locationChannel + "')\">Q1 Q2 Q3</button>").appendTo("#" + channelKey + "_" + locationId);
            });
        });
    });
});
function addChannelAvg(locationKey, locationChannel) {
    overviewChart.data.datasets.push({
        label: locationChannel + " avg",
        data: graphDataChannels[locationChannel].avg,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: false
    });
    overviewChart.update();
}
function addChannelMin(locationKey, locationChannel) {
    overviewChart.data.datasets.push({
        label: locationChannel + " min",
        data: graphDataChannels[locationChannel].min,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: false
    });
    overviewChart.update();
}
function addChannelQ123(locationKey, locationChannel) {
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
    overviewChart.update();
}
function addChannelMax(locationKey, locationChannel) {
    overviewChart.data.datasets.push({
        label: locationChannel + " max",
        data: graphDataChannels[locationChannel].max,
        backgroundColor: channelColours[locationKey].backgroundColor,
        borderColor: channelColours[locationKey].borderColor,
        fill: false
    });
    overviewChart.update();
}