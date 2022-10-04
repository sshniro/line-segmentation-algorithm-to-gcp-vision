## Getting Started

## Installation

Using npm:

The NPM package is available in the following [repository](https://www.npmjs.com/package/line-segmentation-algorithm-to-gcp-vision). Use the following command to install the repository locally.

```js
npm i line-segmentation-algorithm-to-gcp-vision
```

## Usage in Node.js

Input via a JSON file
```JS
const fs = require("fs");
const algorithm = require("line-segmentation-algorithm-to-gcp-vision");

// Load the JSON file which has the GCP Response
const content = fs.readFileSync("./S01200HQT173.jpg.json");
// Parse the JSON fine to a variable
const textJson = JSON.parse(content);

// Perform Line Segmentation
console.log(algorithm.initLineSegmentation(textJson[0]['responses'][0]));
```

Use the response from GCP results

```JS
const algo = require("line-segmentation-algorithm-to-gcp-vision");

// call GCP Vision and retrieve results
let gcpResponse = {};
console.log(algo.initLineSegmentation(gcpResponse[0]['responses'][0]));
```

