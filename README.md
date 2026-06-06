[![npm version](https://img.shields.io/npm/v/line-segmentation-algorithm-to-gcp-vision.svg)](https://www.npmjs.com/package/line-segmentation-algorithm-to-gcp-vision)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](./LICENSE.txt)

# Line Segmentation for Google Cloud Vision OCR

A small library that post-processes Google Cloud Vision OCR output to reconstruct the
correct **line / reading order** for *sparse* documents such as retail receipts and
invoices. Implementations are provided for **Node.js** (`nodejs/`) and **Kotlin** (`kotlin/`).

## Introduction

Google Vision provides 2 options for optical character recognition (OCR).

````
- Option 1: TEXT_DETECTION - Words with coordinates
- Option 2: DOCUMENT_TEXT_DETECTION - OCR on dense text to extract lines and paragraph information
````

The second option is suitable for data extraction from articles (Dense Text such as News Papers/Books). This option has an 
intelligent segmentation method to merge words which are nearby and form lines and paragraphs.
 
This feature is not desirable for images with sparse text content such as retail invoices, where the data relevant to the same line
resides in two corners (A huge gap/whitespace between the product name and price). For these images the OCR segments the 
lines in a different order. If the distance of two words in a single line is too far apart then google vision identifies 
them as two separate paragraphs/lines. 

The below images shows the sample output for a typical invoice from google vision.

<img width="1198" alt="screen shot 2018-01-15 at 3 55 59 pm" src="https://user-images.githubusercontent.com/13045528/34937970-9f2e93b8-fa0c-11e7-9521-0fc6ad191e0d.png">

This behaviour creates a problem in information extraction scenarios. For example, to extract a price of a product from a 
retail invoice the system needs to find a way to match the words in the same line. The algorithm proposed below performs 
line segmentation based on characters polygon coordinates for data extraction.

## Background & motivation

This library was written in **January 2018**. At that time Google Cloud Vision offered only the
two OCR modes above — `TEXT_DETECTION` (words + coordinates) and `DOCUMENT_TEXT_DETECTION`
(dense-text paragraph merging) — and **neither of them reconstructed the correct line / reading
order for sparse layouts** like retail receipts and invoices. When an item label sits at the left
edge of a line and its price at the right edge, Vision treats the two as separate paragraphs and
places the right-hand column at the bottom of the response, breaking the label ↔ price association.

There was no off-the-shelf product that solved this. Google's own platform answer —
**Document AI**, with its Invoice and Expense (receipt) parsers — did not arrive until
**preview in November 2020 and general availability in April 2021**, roughly 2.5–3 years later.
AWS Textract (the nearest competitor that reconstructs lines/tables) only reached GA in
May 2019. So in early 2018, reconstructing the reading order yourself from the word polygon
coordinates — which is exactly what this library does — was the practical option.

## Is there a better way today?

**Yes.** The job this library does by hand is now handled natively by document-understanding
platforms. For new projects, prefer one of these instead:

- **Google Document AI** — the direct successor on Google Cloud. The **Invoice Parser** and
  **Expense (receipt) Parser** return vendor, totals, dates and structured line items directly;
  **Enterprise Document OCR** and the **Layout Parser** return text with correct reading order
  if that is all you need.
- **AWS Textract `AnalyzeExpense`** and **Azure AI Document Intelligence**
  (`prebuilt-invoice` / `prebuilt-receipt`) — equivalent managed invoice/receipt extractors on
  their respective clouds.
- **Multimodal LLMs** (Gemini, GPT-4o, Claude) — send the page image and ask for a JSON
  schema of line items; the model handles OCR *and* reading order in a single step, and reports
  ~87–96% field accuracy on receipts/invoices in recent (2025–2026) benchmarks.
- **Self-hosted / open source** — [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR)
  (PP-StructureV3), [Surya](https://github.com/datalab-to/surya), or
  [docTR](https://github.com/mindee/doctr) if you need to run offline.

This repository is kept as the **original 2018 geometric approach** — useful as a reference
implementation, for understanding the bounding-polygon technique, or as a zero-cost
post-processor if you are already locked into raw Vision `TEXT_DETECTION` output.

## Usage

Usage instructions for each programming language live in the README inside the relevant folder:

- **Node.js** — [`nodejs/ReadME.MD`](./nodejs/ReadME.MD)
- **Kotlin** — [`kotlin/README.md`](./kotlin/README.md)

Both implementations take the **JSON response from a Google Vision OCR call** (the
`AnnotateImageResponse` / `responses[0]` object) as input and return an ordered list of lines.

### Proposed algorithm

The implemented algorithm runs in two stages

- Stage 1 - Groups nearby words to generate a longer strip of line
- Stage 2 - Connects words which are far apart using the bounding polygon approach

<img width="437" alt="screen shot 2018-01-15 at 4 50 31 pm" src="https://user-images.githubusercontent.com/13045528/34940084-415cf57e-fa14-11e7-8099-ffa7fbce1b21.png">


## Explanation

Stage one helps to reduce the computations needed for the second phase of the algorithm. In the first phase the algorithms
tries to merge words/characters which are very near. Stage 1 should be completed because for price related text like $3.40 is presented as 2 words by 
Google Vision (word 1: `$3.` word 2:`,40`). The first stage helps to concat nearby characters to form a text-block/word. 
This step helps reduces the computation needed for the second phase.

The stage 2 algorithm draws an imaginary bounding polygon (with a threshold) over the words and computes the 
words which belongs to each line.

## Known limitations

The code in this repository is intentionally preserved in its original, hand-written form.
The following limitations are documented here rather than patched, so the behaviour is
predictable if you choose to extend it:

- It works well for most slanted and slightly crumpled images, but it will fail on heavily
  crumpled or folded images.
- **Line ordering for three or more fragments is incomplete.** `arrangeWordsInOrder` composes
  two fragments per line (see the `TODO` in `nodejs/index.js`); a line split into three or more
  far-apart pieces may not be ordered left-to-right.
- Stage 2 extends each line's bounding polygon across a **fixed horizontal range (x up to
  2000 px)**; very wide images may need that threshold raised.
- Stage 1 re-associates Vision's word fragments to lines by **string-matching against the
  detected line text**, which assumes Vision emits the words in reading order within each
  detected line.

## Test

##### Node JS

- cd nodejs
- npm install
- npm test

##### Kotlin

- cd kotlin
- ./gradlew test


## Future Work

Try to implement the water-flow algorithm for line segmentation and measure accuracies with bounding polygon approach. 

<img width="211" alt="waterflow" src="https://user-images.githubusercontent.com/13045528/34940259-d6899526-fa14-11e7-9b6c-4b3a2aaa1a75.png">

## License

Released under the [Apache License 2.0](./LICENSE.txt).
