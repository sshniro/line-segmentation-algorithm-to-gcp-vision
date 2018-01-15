const inside = require('point-in-polygon');
const deepcopy = require("deepcopy");

function getYMax(data) {
    let v = data.textAnnotations[0].boundingPoly.vertices;
    let yArray = [];
    for(let i=0; i <4; i++){
        yArray.push(v[i]['y']);
    }
    return Math.max.apply(null, yArray);
}

function invertAxis(data, yMax) {
    data = fillMissingValues(data);
    for(let i=1; i < data.textAnnotations.length; i++ ){
        let v = data.textAnnotations[i].boundingPoly.vertices;
        let yArray = [];
        for(let j=0; j <4; j++){
            v[j]['y'] = (yMax - v[j]['y']);
        }
    }
    return data;
}

function fillMissingValues(data) {
    for(let i=1; i < data.textAnnotations.length; i++ ){
        let v = data.textAnnotations[i].boundingPoly.vertices;
        if(v['x'] == undefined){
            v['x'] = 0;
        }
        if(v['y'] == undefined){
            v['y'] = 0;
        }
    }
    return data;
}

function getBoundingPolygon(mergedArray) {

    for(let i=0; i< mergedArray.length; i++) {
        let arr = [];

        // calculate line height
        let h1 = mergedArray[i].boundingPoly.vertices[0].y - mergedArray[i].boundingPoly.vertices[3].y;
        let h2 = mergedArray[i].boundingPoly.vertices[1].y - mergedArray[i].boundingPoly.vertices[2].y;
        let h = h1;
        if(h2> h1) {
            h = h2
        }
        let avgHeight = h * 0.6;

        arr.push(mergedArray[i].boundingPoly.vertices[1]);
        arr.push(mergedArray[i].boundingPoly.vertices[0]);
        let line1 = getRectangle(deepcopy(arr), true, avgHeight, true);

        arr = [];
        arr.push(mergedArray[i].boundingPoly.vertices[2]);
        arr.push(mergedArray[i].boundingPoly.vertices[3]);
        let line2 = getRectangle(deepcopy(arr), true, avgHeight, false);

        mergedArray[i]['bigbb'] = createRectCoordinates(line1, line2);
        mergedArray[i]['lineNum'] = i;
        mergedArray[i]['match'] = [];
        mergedArray[i]['matched'] = false;
    }

}


function combineBoundingPolygon(mergedArray) {
    // select one word from the array
    for(let i=0; i< mergedArray.length; i++) {

        let bigBB = mergedArray[i]['bigbb'];

        // iterate through all the array to find the match
        for(let k=i; k< mergedArray.length; k++) {
            // Do not compare with the own bounding box and which was not matched with a line
            if(k !== i && mergedArray[k]['matched'] === false) {
                let insideCount = 0;
                for(let j=0; j < 4; j++) {
                    let coordinate = mergedArray[k].boundingPoly.vertices[j];
                    if(inside([coordinate.x, coordinate.y], bigBB)){
                        insideCount += 1;
                    }
                }
                // all four point were inside the big bb
                if(insideCount === 4) {
                    let match = {matchCount: insideCount, matchLineNum: k};
                    mergedArray[i]['match'].push(match);
                    mergedArray[k]['matched'] = true;
                }

            }
        }
    }
}

var exports = module.exports = {};

exports.getYMax = function (data) {
    return getYMax(data);
};

exports.invertAxis = function (data, yMax) {
    return invertAxis(data, yMax);
};

exports.getBoundingPolygon = function (mergedArray) {
    return getBoundingPolygon(mergedArray);
};

exports.combineBoundingPolygon = function (mergedArray) {
    return combineBoundingPolygon(mergedArray);
};
