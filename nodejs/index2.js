const deepCopy = require("deepcopy");
const coordinatesHelper = require('./coordinatesHelper');

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
    let rawText = deepCopy(data.textAnnotations);

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
                finalArray.push(arrangeWordsInOrder(mergedArray, i));
            }
        }
    }
    return finalArray;
}

function getMergedLines(lines,rawText) {
    let mergedArray = [];
    // if(rawText)
    while(lines.length !== 1) {
        let l = lines.pop();
        let l1 = deepCopy(l);
        let status = true;

        let mergedElement;
        while (true) {
            let wElement = rawText.pop();
            if(wElement === undefined) {
                break;
            }
            let w = wElement.description;

            let index = l.indexOf(w);
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
    console.log(mergedArray)
    return mergedArray;
}

function arrangeWordsInOrder(mergedArray, k) {
    let mergedLine = '';
    let ListMatchArray = [];
    ListMatchArray.push(mergedArray[k]);
    mergedArray[k].match.forEach(element => {
        ListMatchArray.push(mergedArray[element.matchLineNum]);
        if(mergedArray[element.matchLineNum].match){
            mergedArray[element.matchLineNum].match.forEach(element2 => {
                ListMatchArray.push(mergedArray[element2.matchLineNum]);
            });
        }
    });
    
    ListMatchArray.sort((a, b) => (a.boundingPoly.vertices[0].x > b.boundingPoly.vertices[0].x) ? 1 : 
        (a.boundingPoly.vertices[0].x === b.boundingPoly.vertices[0].x) ? (
            (a.boundingPoly.vertices[0].y < b.boundingPoly.vertices[0].y) ? 1 : -1) : -1 )
    
            ListMatchArray.forEach(element => {
        mergedLine = mergedLine + ' ' + element.description;
    });
    return mergedLine.substring(1);
}

var exports = module.exports = {};

exports.initLineSegmentation = function (data) {
    return initLineSegmentation(data);
};
