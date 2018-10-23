const fs = require("fs");
const deepcopy = require("deepcopy");
const coordinatesHelper = require('./coordinatesHelper');

const content = fs.readFileSync("../json/S01200HQT173.jpg.json");
const textJson = JSON.parse(content);

initLineSegmentation(textJson[0]['responses'][0]);

/**
 * GCP Vision groups several nearby words to appropriate lines
 * But will not group words that are too far away
 * This function combines nearby words and create a combined bounding polygon
 */
function initLineSegmentation(data) {

    const yMax = coordinatesHelper.getYMax(data);
    data = coordinatesHelper.invertAxis(data, yMax);

    // The first index refers to the auto identified words which belongs to a sings line
    let lines = data.textAnnotations[0].description.split('\n');

    // gcp vision full text
    let rawText = deepcopy(data.textAnnotations);

    // reverse to use lifo, because array.shift() will consume 0(n)
    lines = lines.reverse();
    rawText = rawText.reverse();
    // to remove the zeroth element which gives the total summary of the text
    rawText.pop();

    let mergedArray = getMergedLines(lines, rawText);

    coordinatesHelper.getBoundingPolygon(mergedArray);
    coordinatesHelper.combineBoundingPolygon(mergedArray);

    // This does the line segmentation based on the bounding boxes
    return constructLineWithBoundingPolygon(mergedArray);
}

// TODO implement the line ordering for multiple words
function constructLineWithBoundingPolygon(mergedArray) {
    let finalArray = [];

    for(let i=0; i< mergedArray.length; i++) {
        if(!mergedArray[i]['matched']){
            if(mergedArray[i]['match'].length === 0){
                finalArray.push(mergedArray[i].description)
            }else{
                // arrangeWordsInOrder(mergedArray, i);
                // let index = mergedArray[i]['match'][0]['matchLineNum'];
                // let secondPart = mergedArray[index].description;
                // finalArray.push(mergedArray[i].description + ' ' +secondPart);
                finalArray.push(arrangeWordsInOrder(mergedArray, i));
            }
        }
    }
    return finalArray;
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

function arrangeWordsInOrder(mergedArray, k) {
    let mergedLine = '';
    let wordArray = [];
    let line = mergedArray[k]['match'];
    // [0]['matchLineNum']
    for(let i=0; i < line.length; i++){
        let index = line[i]['matchLineNum'];
        let matchedWordForLine = mergedArray[index].description;

        let mainX = mergedArray[k].boundingPoly.vertices[0].x;
        let compareX = mergedArray[index].boundingPoly.vertices[0].x;

        if(compareX > mainX) {
            mergedLine = mergedArray[k].description + ' ' + matchedWordForLine;
        }else {
            mergedLine = matchedWordForLine + ' ' + mergedArray[k].description;
        }
    }
    return mergedLine;
}

var exports = module.exports = {};

exports.initLineSegmentation = function (data) {
    return initLineSegmentation(data);
};
