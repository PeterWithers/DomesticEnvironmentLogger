/*
 Copyright (C) 2020 Peter Withers
 
 @since 02/01/2020 21:08 PM (creation date)
 @author @author : Peter Withers <peter-gthb@bambooradical.com>
 */

$(document).ready(function () {
    $("<table id=\"radioDataTable\"/>").appendTo("body");
    $("<tr id=\"headerRow\"><td id=\"emptyCell\"/></tr>").appendTo("#radioDataTable");
    $.getJSON("/monitor/listRadioData", function (radioDataArray) {
        $.each(radioDataArray, function (index, radioData) {
            var radioDataIdex = "radioData" + index;
            $("<tr><td>" + radioData.location + " " + radioData.recordDate + "</td></tr><tr id=\"" + radioDataIdex + "\"></tr>").appendTo("#radioDataTable");
            $.each(radioData.dataValues.split(" "), function (splitIndex, value) {
                $("<td>" + value + "</td>").appendTo("#" + radioDataIdex);
            });
        });
    });
});
