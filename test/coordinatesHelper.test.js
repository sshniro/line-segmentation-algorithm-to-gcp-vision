const coordinatesHelper = require('../coordinatesHelper');

test('it should return the max Y vertice', () => {
    const vertices = {"textAnnotations":[{"boundingPoly":{
        "vertices":[
            {"x":262,"y":260},
            {"x":1176,"y":260},
            {"x":1176,"y":3486},
            {"x":262,"y":3486}
        ]}}]};
    const yMax = coordinatesHelper.getYMax(vertices);
    expect(yMax).toBe(3486);
});

test('it should fill all undefined vertices x and y cordinates', () => {
    const vertices = {"textAnnotations":[
    {"boundingPoly":{
        "vertices":[
            {"x":262,"y":260},
            {"x":1176,"y":260},
            {"x":1176,"y":3486},
            {"x":262,"y":3486}
        ]
    }},
    {"boundingPoly":{
        "vertices":[
            {"x":262,"y":undefined},
            {"x":1176,"y":260},
            {"x":undefined,"y":3486},
            {"x":262,"y":3486}
        ]
    }}]};

    const matchingVal = [   
        { x: 262, y: 3486 },
        { x: 1176, y: 3226 },
        { x: 0, y: 0 },
        { x: 262, y: 0 } 
    ];
    const maxYVertice = 3486;
    const invertedData = coordinatesHelper.invertAxis(vertices,maxYVertice);
    expect(invertedData.textAnnotations[1].boundingPoly.vertices).toEqual(matchingVal);
});
