/*
 Copyright (C) 2020 Peter Withers
 
 @since 02/01/2020 21:08 PM (creation date)
 @author @author : Peter Withers <peter-gthb@bambooradical.com>
 */

$(document).ready(function () {
    $("<table id=\"radioDataTable\"/>").appendTo("body");
    $("<tr id=\"headerRow\"><td id=\"emptyCell\"/></tr>").appendTo("#radioDataTable");
    $.getJSON("/monitor/listRadioData", function (radioDataArray) {
        Object.keys(radioDataArray).sort().forEach(function (index, radioData) {
            var locationId = radioData.location.replace(/ /g, "_");
            $("<tr id=\"" + locationId + "\"><td>" + radioData.location + "</td><td>" + radioData.recordDate + "</td></tr>").appendTo("#radioDataTable");
            for (var value in dataValues) {
                $("<td>" + value +"</>").appendTo("#" + locationId);
            }
        });
    });
});
