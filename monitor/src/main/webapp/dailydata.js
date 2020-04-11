/*
 Copyright (C) 2020 Peter Withers
 
 @since 09-04-2020 21:48 PM (creation date)
 @author : Peter Withers <peter-gthb@bambooradical.com>
 */
var dailyChart;
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
    'aquariumA1': {
        label: 'aquariuma1',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'aquariumA0': {
        label: 'aquariuma0',
        backgroundColor: "rgba(75, 92, 192, 0.2)",
        borderColor: "rgba(75, 92, 192, 1)"},
    'third testing messaging': {
        label: 'third testing messaging',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'second testing board bg': {
        label: 'second testing board',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
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
    'second top floorA0': {
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
        backgroundColor: "rgba(255,99,132, 0.2)",
        borderColor: "rgba(255,99,132, 1)"},
    'invalid-second top floor': {
        label: 'invalid-second top floor',
        backgroundColor: "rgba(179,181,198, 0.2)",
        borderColor: "rgba(179,181,198, 1)"},
    'third ground floor': {
        label: 'third ground floor',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'third testing board': {
        label: 'third ground floor',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'third testing board bg': {
        label: 'third ground floor',
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)"},
    'windspeeddeelen': {
        label: 'windspeeddeelen',
        backgroundColor: "rgba(123,123,123, 0.2)",
        borderColor: "rgba(123,123,123, 1)"},
    'air_monitor_01': {
        label: 'windspeeddeelen',
        backgroundColor: "rgba(211,118,100, 0.2)",
        borderColor: "rgba(211,118,100, 1)"},
};

$(document).ready(function () {
    $("<canvas id=\"dailyContainer\" width=\"800\" height=\"400\"></canvas>").appendTo("body");
    var dailyContainer = $("#dailyContainer");
//    var dailyContainer = document.getElementById('dailyContainer').getContext('2d');
    dailyChart = new Chart(dailyContainer, {
        type: 'line',
        data: {
            datasets: []
        },
        options: {
            legend: {
                display: false,
            },
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
    $("<tr><td><input type=\"number\" id=\"spanDays\" value=\"7\"/></td><td><input type=\"number\" id=\"startDay\" value=\"-1\"/></td></tr>").appendTo("#buttonsTable");
    $("<tr><td><button onclick=\"calculateData()\">calculateData</button></td></tr>").appendTo("#buttonsTable");
});

function calculateData() {
    dailyChart.data.datasets = [];
    graphDataChannels = {};
    var endDate = new Date();
    endDate.setDate(endDate.getDate() + parseInt($("#startDay").val()));
    var startDate = new Date();
    startDate.setDate(endDate.getDate() - parseInt($("#spanDays").val()));
    for (var currentDate = startDate; currentDate <= endDate; currentDate.setDate(currentDate.getDate() + 1)) {
        $.getJSON("/monitor/DayOfData" + currentDate.getFullYear() + "-" + (currentDate.getMonth() + 1) + "-" + currentDate.getDate() + ".json", function (locationData) {
            Object.keys(locationData).sort().forEach(function (locationKey) {
                var channelData = locationData[locationKey];
                var locationString = channelData.location;
                console.log(locationString);
                var locationId = channelData.location.replace(/ /g, "_");
                var dateX = new Date(channelData.recordDate);
                if (typeof graphDataChannels[locationId] === 'undefined') {
                    graphDataChannels[locationId] = {
                        "temperature": [],
                        "humidity": [],
                        "dustAvg": [],
                        "dustQ1": [],
                        "dustQ2": [],
                        "dustQ3": [],
                    };
                    if ($("#" + locationId).length == 0) {
                        $("<tr id=\"" + locationId + "\"><td>" + locationId + "</td></tr>").appendTo("#buttonsTable");
                        $("<td id=\"temperature_" + locationId + "\">").appendTo("#" + locationId);
                        $("<td id=\"humidity_" + locationId + "\">").appendTo("#" + locationId);
                        $("<td id=\"dustAvg_" + locationId + "\">").appendTo("#" + locationId);
                        $("<td id=\"dustQ1_" + locationId + "\">").appendTo("#" + locationId);
                        $("<td id=\"dustQ2_" + locationId + "\">").appendTo("#" + locationId);
                        $("<td id=\"dustQ3_" + locationId + "\">").appendTo("#" + locationId);
                        $("<button hidden=\"true\" style=\"margin: 0px 2px 0px 2px; border: " + channelColours[locationString].borderColor + " 3px solid; background:" + channelColours[locationString].backgroundColor + ";\" onclick=\"addChannel('" + locationId + "', '" + locationString + "', 'temperature')\">temperature</button>").appendTo("#temperature_" + locationId);
                        $("<button hidden=\"true\" style=\"margin: 0px 2px 0px 2px; border: " + channelColours[locationString].borderColor + " 3px solid; background:" + channelColours[locationString].backgroundColor + ";\" onclick=\"addChannel('" + locationId + "', '" + locationString + "', 'humidity')\">humidity</button>").appendTo("#humidity_" + locationId);
                        $("<button hidden=\"true\" style=\"margin: 0px 2px 0px 2px; border: " + channelColours[locationString].borderColor + " 3px solid; background:" + channelColours[locationString].backgroundColor + ";\" onclick=\"addChannel('" + locationId + "', '" + locationString + "', 'dustAvg')\">dustAvg</button>").appendTo("#dustAvg_" + locationId);
                        $("<button hidden=\"true\" style=\"margin: 0px 2px 0px 2px; border: " + channelColours[locationString].borderColor + " 3px solid; background:" + channelColours[locationString].backgroundColor + ";\" onclick=\"addChannel('" + locationId + "', '" + locationString + "', 'dustQ1')\">dustQ1</button>").appendTo("#dustQ1_" + locationId);
                        $("<button hidden=\"true\" style=\"margin: 0px 2px 0px 2px; border: " + channelColours[locationString].borderColor + " 3px solid; background:" + channelColours[locationString].backgroundColor + ";\" onclick=\"addChannel('" + locationId + "', '" + locationString + "', 'dustQ2')\">dustQ2</button>").appendTo("#dustQ2_" + locationId);
                        $("<button hidden=\"true\" style=\"margin: 0px 2px 0px 2px; border: " + channelColours[locationString].borderColor + " 3px solid; background:" + channelColours[locationString].backgroundColor + ";\" onclick=\"addChannel('" + locationId + "', '" + locationString + "', 'dustQ3')\">dustQ3</button>").appendTo("#dustQ3_" + locationId);
                    }
                }
                if (channelData.temperature !== undefined) {
                    $("#temperature_" + locationId).children().show();
                }
                if (channelData.humidity !== undefined) {
                    $("#humidity_" + locationId).children().show();
                }
                if (channelData.dustAvg !== undefined) {
                    $("#dustAvg_" + locationId).children().show();
                }
                if (channelData.dustQ1 !== undefined) {
                    $("#dustQ1_" + locationId).children().show();
                }
                if (channelData.dustQ2 !== undefined) {
                    $("#dustQ2_" + locationId).children().show();
                }
                if (channelData.dustQ3 !== undefined) {
                    $("#dustQ3_" + locationId).children().show();
                }
                graphDataChannels[locationId]["temperature"].push({'x': dateX, 'y': channelData.temperature});
                graphDataChannels[locationId]["humidity"].push({'x': dateX, 'y': channelData.humidity});
                graphDataChannels[locationId]["dustAvg"].push({'x': dateX, 'y': channelData.dustAvg});
                graphDataChannels[locationId]["dustQ1"].push({'x': dateX, 'y': channelData.dustQ1});
                graphDataChannels[locationId]["dustQ2"].push({'x': dateX, 'y': channelData.dustQ2});
                graphDataChannels[locationId]["dustQ3"].push({'x': dateX, 'y': channelData.dustQ3});
            });
        });
    };
}

function addChannel(locationId, locationString, dataChannel) {
    console.log(locationId);
    console.log(dataChannel);
    graphDataChannels[locationId][dataChannel].sort(function (a, b) {
        return a.x - b.x
    });
    dailyChart.data.datasets.push({
        label: locationId + "_" + dataChannel,
        data: graphDataChannels[locationId][dataChannel],
        backgroundColor: channelColours[locationString].backgroundColor,
        borderColor: channelColours[locationString].borderColor,
        fill: false,
        pointRadius: 0
    });
    dailyChart.update();
}