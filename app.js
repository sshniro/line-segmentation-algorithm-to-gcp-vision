const fs = require("fs");
const deepcopy = require("deepcopy");
const _ = require('lodash');

const coordinatesHelper = require('./coordinatesHelper');


const content = fs.readFileSync("./json/S01200HS22A9.jpeg.json");
const textJson = JSON.parse(content);
mergeNearByWords(textJson);


/**
 * GCP Vision groups several nearby words to appropriate lines
 * But will not group words that are too far away
 * This function combines nearby words and create a combined bounding polygon
 */

function mergeNearByWords(data) {

    const yMax = coordinatesHelper.getYMax(data);
    data = coordinatesHelper.invertAxis(data, yMax);

    // Auto identified and merged lines from gcp vision
    let lines = data.textAnnotations[0].description.split('\n');
    // gcp vision full text
    let rawText = deepcopy(data.textAnnotations);

    // reverse to use lifo, because array.shift() will consume 0(n)
    lines = lines.reverse();
    rawText = rawText.reverse();
    // to remove the zeroth element which gives the total summary of the text
    rawText.pop();

    let mergedArray = getMergedLines(lines, rawText);
}

function getMergedLines(lines,rawText) {

    let mergedArray = [];
    while(lines.length !== 1) {
        let l = lines.pop();
        let l1 = deepcopy(l);
        let status = true;

        let data = "";
        let mergedElement;

        while (true) {
            let wElement = rawText.pop();
            if(wElement === undefined) {
                break;
            }
            let w = wElement.description;

            let index = l.indexOf(w);
            let temp;
            // check if the word is inside
            l = l.substring(index + w.length);
            if(status) {
                status = false;
                // set starting coordinates
                mergedElement = wElement;
            }
            if(l === ""){
                // set ending coordinates
                mergedElement.description = l1;
                mergedElement.boundingPoly.vertices[1] = wElement.boundingPoly.vertices[1];
                mergedElement.boundingPoly.vertices[2] = wElement.boundingPoly.vertices[2];
                mergedArray.push(mergedElement);
                break;
            }
        }
    }
    return mergedArray;
}
