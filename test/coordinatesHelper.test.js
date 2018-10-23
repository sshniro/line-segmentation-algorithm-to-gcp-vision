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

