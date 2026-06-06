# Line Segmentation — Node.js

Post-processes a Google Cloud Vision OCR response and returns the text as an ordered list of
lines, reconstructing reading order for sparse documents (receipts/invoices).

> **Note:** this is a 2018 library, kept as a reference implementation. For new projects, see
> [*Is there a better way today?*](../README.md#is-there-a-better-way-today) in the root README.

## Installation

Using npm — the package is published
[on npm](https://www.npmjs.com/package/line-segmentation-algorithm-to-gcp-vision):

```bash
npm i line-segmentation-algorithm-to-gcp-vision
```

## Input

The library does **not** call Google Vision itself — it only post-processes the response you
already have. It expects a single Vision `AnnotateImageResponse` object (i.e. one element of
`responses`), which must contain a `textAnnotations` array where:

- `textAnnotations[0]` is the full-text summary block (description + page bounding box), and
- `textAnnotations[1..n]` are the individual words, each with `description` and
  `boundingPoly.vertices`.

This is what a `TEXT_DETECTION` (or `DOCUMENT_TEXT_DETECTION`) request returns.

## Usage

Input via a JSON file:

```JS
const fs = require("fs");
const algorithm = require("line-segmentation-algorithm-to-gcp-vision");

// Load the JSON file which has the GCP Response
const content = fs.readFileSync("./S01200HQT173.jpg.json");
// Parse the JSON file to a variable
const textJson = JSON.parse(content);

// Perform Line Segmentation -> returns an ordered array of line strings
console.log(algorithm.initLineSegmentation(textJson[0]['responses'][0]));
```

Using the response directly from a GCP Vision call (install `@google-cloud/vision` separately to
fetch OCR results — this library does not depend on it):

```JS
const vision = require("@google-cloud/vision");
const algo = require("line-segmentation-algorithm-to-gcp-vision");

const client = new vision.ImageAnnotatorClient();
const [result] = await client.textDetection("./receipt.jpg");

// `result` is a single AnnotateImageResponse (it exposes `.textAnnotations` directly)
console.log(algo.initLineSegmentation(result));
```

## Output

An array of strings, one per reconstructed line, e.g.:

```js
[ 'KITTEN FOOD £3.50', 'DIPPERS £2.50', 'CKN DIPPERS £1.50', ... ]
```

## Test

```
npm install
npm test
```

The tests must be run from inside the `nodejs/` directory (the fixture is loaded with a path
relative to that folder).
