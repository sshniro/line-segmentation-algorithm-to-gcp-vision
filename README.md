# Introduction

Google vision outperforms most of the ocr providers. It provides two options for OCR capabilities.

- TEXT_DETECTION - Word output with coordinates
- DOCUMENT_TEXT_DETECTION - OCR on dense text to extract lines and paragraph information

The second option is good for data extraction from normal articles but for content like invoice and receipts if the distance is too far apart the google vision identifies them as seperate paragraphs. The below images shows the sample output for a typical invoice from google vision.

<img width="1198" alt="screen shot 2018-01-15 at 3 55 59 pm" src="https://user-images.githubusercontent.com/13045528/34937970-9f2e93b8-fa0c-11e7-9521-0fc6ad191e0d.png">

The problem arrises in infromation extraction scenarios when extraction what are the prices for certian products.The algorithm proposed below does line segmentation based on characters polygon coordinates for data extraction.

### Proposed Algorithm

The implemented algorithm runs in two stages
- Stage 1 - Groups nearby words to generate a longer strip of line
- Stage 2 - Segments faraway words using the bounding polygon approach.

<img width="437" alt="screen shot 2018-01-15 at 4 50 31 pm" src="https://user-images.githubusercontent.com/13045528/34940084-415cf57e-fa14-11e7-8099-ffa7fbce1b21.png">


## Explanation.

Stage 1 should be completed because for price related text like $3.40 is presented as 2 words by Google Vision ($3.,40). The first stage helps to concat nearby characters to form a text-block/word. This step helps reduces the computation needed for the second phase.

The stage 2 algorithm draws a imaginery bounding polygon with a threshold over words and computes the words which belongs to each lines.

## Issues.

The algorithm succesfully works for most of the slanted and slightly crumpled images. But it will fail to highly crumpled or folded images.

## Future Work

Try to implement the waterflow algorithm for line segmentation and measure accuracies with bouding polygon approach. 

<img width="211" alt="waterflow" src="https://user-images.githubusercontent.com/13045528/34940259-d6899526-fa14-11e7-9b6c-4b3a2aaa1a75.png">
