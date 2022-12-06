
let VisaStampType = {
    ARROW: "ARROW",
    CIRCLE: "CIRCLE",
    DIAMOND: "DIAMOND",
    ELLIPSE: "ELLIPSE",
    HEXAGON: "HEXAGON",
    INDENTED_RECTANGLE: "INDENTED_RECTANGLE",
    ROUNDED_RECTANGLE: "ROUNDED_RECTANGLE",
    ROUNDED_TRIANGLE: "ROUNDED_TRIANGLE"
};
let DateFormatType = {
    ISO_DATE: "ISO_DATE",
    SHORT_DATE: "SHORT_DATE",
    LONG_DATE: "LONG_DATE"
};
const svgns = "http://www.w3.org/2000/svg";
function generateSVG(div) {
    let svg = document.createElementNS(svgns, 'svg');
}
function generateDateString(dateString, dateFormat) {
    let date = new Date(dateString);
    console.log("generateDateString for " + dateString + " becomes " + date);
    const months = ["Jan", "Feb", "Mar","Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    let year = "" + date.getFullYear();
    let month = "" + months[date.getMonth()];
    let day = "" + date.getDate();
    if (dateFormat === DateFormatType.ISO_DATE) {
        dateString = year + "-" + date.getMonth() + " " + day;
    }
    else if (dateFormat === DateFormatType.LONG_DATE) {
        dateString = "" + (date.getMonth()+1) + "/" + day + "/" + year;
    }
    else { // DateFormatType.SHORT_DATE
        dateString = "" + month + " " + day + " " + year;
    }
    return dateString;
}
function initSVGColor(svg, fill, stroke, strokeWidth) {
    console.log("initSVGColor fill: " + fill);
    console.log("initSVGColor stroke: " + stroke);
    console.log("initSVGColor strokeWidth: " + strokeWidth);
    svg.setAttributeNS(null, 'fill', fill)
    svg.setAttributeNS(null, 'stroke', stroke);
    svg.setAttributeNS(null, 'stroke-width', strokeWidth);
}
function addPoint(svgPane, polygon, x, y) {
    let point = svgPane.createSVGPoint();
    point.x = x;
    point.y = y;
    polygon.points.appendItem(point);
}
function generateArrow(paneLength, svgPane, color, borderThickness) {
    let arrow = document.createElementNS(svgns, 'polygon');
    addPoint(svgPane, arrow, -paneLength/3, -paneLength/4);
    addPoint(svgPane, arrow, paneLength/2.5, -paneLength/4);
    addPoint(svgPane, arrow, paneLength/3, 0);
    addPoint(svgPane, arrow, paneLength/2.5, paneLength/4);
    addPoint(svgPane, arrow, -paneLength/3, paneLength/4);
    addPoint(svgPane, arrow, -paneLength/2.5, 0);
    initSVGColor(arrow, '#00000000', color, borderThickness);
    return arrow;
}
function generateCircle(paneLength, svgPane, color, borderThickness) {
    let min = Math.min(paneLength);
    let circle = document.createElementNS(svgns, 'circle');
    circle.setAttributeNS(null, 'cx', 0);
    circle.setAttributeNS(null, 'cy', 0);
    circle.setAttributeNS(null, 'r', min / 2.1);    
    initSVGColor(circle, '#00000000', color, borderThickness);
    return circle;
}
function generateDiamond(paneLength, svgPane, color, borderThickness) {
    let min = Math.min(paneLength);
    let diamond = document.createElementNS(svgns, 'polygon');
    addPoint(svgPane, diamond, 0, -min/2.1);
    addPoint(svgPane, diamond, min/2.1, 0);
    addPoint(svgPane, diamond, 0, min/2.1);
    addPoint(svgPane, diamond, -min/2.1, 0);
    initSVGColor(diamond, '#00000000', color, borderThickness);
    return diamond;
}
function generateEllipse(paneLength, svgPane, color, borderThickness) {
    let ellipse = document.createElementNS(svgns, 'ellipse');
    ellipse.setAttributeNS(null, 'cx', 0);
    ellipse.setAttributeNS(null, 'cy', 0);
    ellipse.setAttributeNS(null, 'rx', paneLength / 2.2);
    ellipse.setAttributeNS(null, 'ry', paneLength / 4);
    initSVGColor(ellipse, '#00000000', color, borderThickness);
    return ellipse;
}
function generateHexagon(paneLength, svgPane, color, borderThickness) {
    let hexagon = document.createElementNS(svgns, 'polygon');
    addPoint(svgPane, hexagon, 0, -paneLength/2.3);
    addPoint(svgPane, hexagon, paneLength/2.3, -paneLength/6);
    addPoint(svgPane, hexagon, paneLength/2.3, paneLength/6);
    addPoint(svgPane, hexagon, 0, paneLength/2.3);
    addPoint(svgPane, hexagon, -paneLength/2.3, paneLength/6);
    addPoint(svgPane, hexagon, -paneLength/2.3, -paneLength/6);
    initSVGColor(hexagon, '#00000000', color, borderThickness);
    return hexagon;
}
function generateIndentedRectangle(paneLength, svgPane, color, borderThickness) {
    let indentedRectangle = document.createElementNS(svgns, 'polygon');
    addPoint(svgPane, indentedRectangle, -paneLength/2.6, -paneLength/3);
    addPoint(svgPane, indentedRectangle, paneLength/2.6, -paneLength/3);
    addPoint(svgPane, indentedRectangle, paneLength/3, 0);
    addPoint(svgPane, indentedRectangle, paneLength/2.6, paneLength/3);
    addPoint(svgPane, indentedRectangle, -paneLength/2.6, paneLength/3);
    addPoint(svgPane, indentedRectangle, -paneLength/3, 0);
    initSVGColor(indentedRectangle, '#00000000', color, borderThickness);
    initSVGColor(indentedRectangle, '#00000000', color, borderThickness);
    return indentedRectangle;
}
function generateRoundedRectangle(paneLength, svgPane, color, borderThickness) {
    let roundedRectangle = document.createElementNS(svgns, 'rect');
    roundedRectangle.setAttributeNS(null, 'x', -paneLength * 3 / 8);
    roundedRectangle.setAttributeNS(null, 'y', -paneLength * 3 / 10);
    roundedRectangle.setAttributeNS(null, 'width', paneLength * 3 / 4);
    roundedRectangle.setAttributeNS(null, 'height', paneLength * 3 / 5);
    roundedRectangle.setAttributeNS(null, 'rx', 15);
    initSVGColor(roundedRectangle, '#00000000', color, borderThickness);
    return roundedRectangle;
}
function generateRoundedTriangle(paneLength, svgPane, color, borderThickness) {
    let roundedTriangle = document.createElementNS(svgns, 'polygon');
    addPoint(svgPane, roundedTriangle, 0, -paneLength/3.5);
    addPoint(svgPane, roundedTriangle, paneLength/2.5, paneLength/3.5);
    addPoint(svgPane, roundedTriangle, -paneLength/2.5, paneLength/3.5);

    // A ROUNDED TRIANGLE NEEDS 6 POINTS IN ITS PATH WITH THREE ARCS
/*    let trianglePoints = [  0,                  (-paneLength/3.5),
                            (paneLength/2.5),    (paneLength/3.5),
                            (-paneLength/2.5),   (paneLength/3.5) ];
    let line1XDiff = trianglePoints[2] - trianglePoints[0];
    let line1YDiff = trianglePoints[3] - trianglePoints[1];
    let line1Slope = line1YDiff/line1XDiff;
    let line1B = trianglePoints[1] - (line1Slope * trianglePoints[0]);

    let p1X = paneLength*.1;
*/
/*    let roundedTriangle = document.createElementNS(svgns, 'path');
    roundedTriangle.setAttributeNS(null, 'd',   'M ' + (paneLength*.1) + ' ' + (-paneLength/3.5) + ' '
                                            +   'L ' + (paneLength/2.5) + ' ' + (paneLength/3.5) + ' '
                                            +   'A ' + 
                                            +   'S 1.3 3 -1.82 3.22 '
                                            +   'L -5.4 0 '
                                            +   'S -3.28 -.14 -1.74 -3.26 '
                                            +   'L 2.76 -4.7 '
                                            +   'S 1.7 -2.3 3.4 0 '
                                            +   'Z');
*/
    // ADD POINTS HERE
    initSVGColor(roundedTriangle, '#00000000', color, borderThickness);
    return roundedTriangle;
}
function transformSVG(svg, paneLength, translationX, translationY, rotation, scale) {
    let cX = paneLength/2;
    let cY = paneLength/2;
    let s = " scale(" + scale + ") ";
    let relT = " translate(" + translationX + ", " + translationY + ") ";
    let r = " rotate(" + rotation + ") ";
    let t = " translate(" + cX + "," + cY + ") ";
    let combinedTransform = t + r + relT + s;
    svg.setAttributeNS(null, 'transform', combinedTransform);
}
function generateStamp(svgPaneId, stampType, regionName, stampLength, nameFontSize, nameY, date, dateFormat,
            dateFontSize, dateY, fontFamily, color, borderThickness, translationX, translationY, rotation) {
    let svgPane = document.getElementById(svgPaneId);

    // FIRST CLEAR THE DISPLAY PANE
    svgPane.innerHTML = "";
    let paneLength = svgPane.getBoundingClientRect().width;
    svgPane.setAttribute("width", stampLength);//paneLength);
    svgPane.setAttribute("height", stampLength);//paneLength);
    borderThickness = borderThickness * paneLength/300;
    switch (stampType) {
        case VisaStampType.ARROW:
            let outerArrow = generateArrow(stampLength, svgPane, color, borderThickness);
            console.log("outerArrow: " + JSON.stringify(outerArrow));
            transformSVG(outerArrow, stampLength, translationX, translationY, rotation, 1.0);
            svgPane.appendChild(outerArrow);
            let innerArrow = generateArrow(stampLength, svgPane, color, borderThickness);
            transformSVG(innerArrow, stampLength, translationX, translationY, rotation, 0.95);
            svgPane.appendChild(innerArrow);
            break;
        case VisaStampType.CIRCLE:
            let outerCircle = generateCircle(paneLength, svgPane, color, borderThickness);
            transformSVG(outerCircle, paneLength, 0, 0, 30, 1.00);
            svgPane.appendChild(outerCircle);
            let innerCircle = generateCircle(paneLength, svgPane, color, borderThickness);
            transformSVG(innerCircle, paneLength, 0, 0, 0, 0.95);
            svgPane.appendChild(innerCircle);
            break;
        case VisaStampType.DIAMOND:
            let outerDiamond = generateDiamond(paneLength, svgPane, color, borderThickness);
            transformSVG(outerDiamond, paneLength, translationX, translationY, rotation, 1.0);
            svgPane.appendChild(outerDiamond);
            let innerDiamond = generateDiamond(paneLength, svgPane, color, borderThickness);
            transformSVG(innerDiamond, paneLength, translationX, translationY, rotation, 0.95);
            svgPane.appendChild(innerDiamond);
            break;
        case VisaStampType.ELLIPSE:
            let outerEllipse = generateEllipse(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(outerEllipse);
            transformSVG(outerEllipse, paneLength, translationX, translationY, rotation, 1.0);
            let innerEllipse = generateEllipse(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(innerEllipse);
            transformSVG(innerEllipse, paneLength, translationX, translationY, rotation, 0.95);
            break;
        case VisaStampType.HEXAGON:
            let outerHex = generateHexagon(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(outerHex);
            transformSVG(outerHex, paneLength, translationX, translationY, rotation, 1.0);
            let innerHex = generateHexagon(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(innerHex);
            transformSVG(innerHex, paneLength, translationX, translationY, rotation, 0.95);
            break;
        case VisaStampType.INDENTED_RECTANGLE:
            let outerIndentedRectangle = generateIndentedRectangle(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(outerIndentedRectangle);
            transformSVG(outerIndentedRectangle, paneLength, translationX, translationY, rotation, 1.0);
            let innerIndentedRectangle = generateIndentedRectangle(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(innerIndentedRectangle);
            transformSVG(innerIndentedRectangle, paneLength, translationX, translationY, rotation, 0.95);
            break;
        case VisaStampType.ROUNDED_RECTANGLE:
            let outerRoundedRectangle = generateRoundedRectangle(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(outerRoundedRectangle);
            transformSVG(outerRoundedRectangle, paneLength, translationX, translationY, rotation, 1.0);
            let innerRoundedRectangle = generateRoundedRectangle(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(innerRoundedRectangle);
            transformSVG(innerRoundedRectangle, paneLength, translationX, translationY, rotation, 0.95);
            break;
        case VisaStampType.ROUNDED_TRIANGLE:
            let outerRoundedTriangle = generateRoundedTriangle(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(outerRoundedTriangle);
            transformSVG(outerRoundedTriangle, paneLength, translationX, translationY, rotation, 10.0);
            let innerRoundedTriangle = generateRoundedTriangle(paneLength, svgPane, color, borderThickness);
            svgPane.appendChild(innerRoundedTriangle);
            transformSVG(innerRoundedTriangle, paneLength, translationX, translationY, rotation, 0.95);
            break;
    }

    let dimBase = 300;
    let textScale = paneLength/dimBase;

    // ADD THE REGION NAME
    let regionNameText = document.createElementNS(svgns, 'text');
    regionNameText.textContent = regionName;
    regionNameText.setAttributeNS(null, 'font-family', fontFamily);
    regionNameText.setAttributeNS(null, 'stroke', color);
    regionNameText.setAttributeNS(null, 'fill', color);
    regionNameText.setAttributeNS(null, 'style', 'font-size:' + nameFontSize + 'pt');
    svgPane.appendChild(regionNameText);
    let regionTextDimensions = regionNameText.getBBox();
    let regionNameTextX = regionTextDimensions.width / 2;
    let regionNameTextY = regionTextDimensions.height / 2;
    regionNameText.setAttribute('x', - regionNameTextX);
    regionNameText.setAttribute('y', - (regionNameTextY/2));
    let nameTranslationY = new Number(translationY) + new Number(nameY);
    transformSVG(regionNameText, paneLength, translationX, nameTranslationY, rotation, textScale);

    // ADD THE DATE
    let dateText = document.createElementNS(svgns, 'text');
    let dateString = generateDateString(date, dateFormat);
    dateText.textContent = dateString;
    dateText.setAttributeNS(null, 'font-family', fontFamily);
    dateText.setAttributeNS(null, 'stroke', color);
    dateText.setAttributeNS(null, 'fill', color);
    dateText.setAttributeNS(null, 'style', 'font-size:' + dateFontSize + 'pt');
    svgPane.appendChild(dateText);
    let dateTextDimensions = dateText.getBBox();
    let dateTextX = dateTextDimensions.width / 2;
    let dateTextY = dateTextDimensions.height / 2;
    dateText.setAttribute('x', - dateTextX);
    dateText.setAttribute('y',   (dateTextY/2));
    let dateTranslationY = new Number(translationY) + new Number(dateY);
    transformSVG(dateText, paneLength, translationX, dateTranslationY, rotation, textScale);
}