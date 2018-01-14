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

var exports = module.exports = {};

exports.getYMax = function (data) {
    return getYMax(data);
};

exports.invertAxis = function (data, yMax) {
    return invertAxis(data, yMax);
};
