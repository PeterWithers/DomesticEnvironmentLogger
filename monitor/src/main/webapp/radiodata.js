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
            $("<tr><td>" + /*radioData.location + " " +*/ radioData.recordDate + "</td></tr><tr><td id=\"" + radioDataIdex + "\"/></tr>").appendTo("#radioDataTable");
            var pathAttribute = "M0 60";
            var totalLength = 0;
            $.each(radioData.dataValues.split(" "), function (splitIndex, value) {
                totalLength += (value/100);
                pathAttribute += " V " + ((splitIndex % 2 == 0)? "0" : "60");
                pathAttribute += " h " + (value/100);
            });
            var pathElement = $(document.createElementNS('http://www.w3.org/2000/svg','path')).attr({d:pathAttribute,stroke:'green','stroke-width':1,fill:'none'});
            var svgElement = $(document.createElementNS('http://www.w3.org/2000/svg','svg')).attr({height:60,width:totalLength});
            pathElement.appendTo(svgElement);
            svgElement.appendTo("#" + radioDataIdex);
        });
    });
});
