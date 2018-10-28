[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/sshniro/line-segmentation-algorithm-to-gcp-vision/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/sshniro/line-segmentation-algorithm-to-gcp-vision.svg?branch=master)](https://travis-ci.com/sshniro/line-segmentation-algorithm-to-gcp-vision)
# Introduction

Google vision outperforms most of the cloud ocr providers. It provides two options for OCR capabilities.

- TEXT_DETECTION - Words with coordinates
- DOCUMENT_TEXT_DETECTION - OCR on dense text to extract lines and paragraph information


The second option is preferred for data extraction from normal articles (Dense Text eg- News Papers, Books). But for 
images with sparse text content such as retails invoices the OCR segments the lines in a different order. If the 
distance of two words in a single line is too far apart then google vision identifies them as two separate paragraphs/lines. 

The below images shows the sample output for a typical invoice from google vision.

<img width="1198" alt="screen shot 2018-01-15 at 3 55 59 pm" src="https://user-images.githubusercontent.com/13045528/34937970-9f2e93b8-fa0c-11e7-9521-0fc6ad191e0d.png">

This behaviour creates a problem in information extraction scenarios. For example when reading a retail invoice and 
extracting the relevant price for the products. The algorithm proposed below provides line segmentation based on characters 
polygon coordinates for data extraction.

### Proposed Algorithm

The implemented algorithm runs in two stages
- Stage 1 - Groups nearby words to generate a longer strip of line
- Stage 2 - Connects words which are far apart using the bounding polygon approach

<img width="437" alt="screen shot 2018-01-15 at 4 50 31 pm" src="https://user-images.githubusercontent.com/13045528/34940084-415cf57e-fa14-11e7-8099-ffa7fbce1b21.png">


## Explanation.

Stage 1 should be completed because for price related text like $3.40 is presented as 2 words by 
Google Vision (word 1: $3. word 2:,40). The first stage helps to concat nearby characters to form a text-block/word. 
This step helps reduces the computation needed for the second phase.

The stage 2 algorithm draws an imaginary bounding polygon (with a threshold) over the words and computes the 
words which belongs to each line.

## Issues.

The algorithm successfully works for most of the slanted and slightly crumpled images. But it will fail to highly 
crumpled or folded images.

## Usage 
##### Node JS

- cd nodejs
- npm install
- npm test


## Future Work

Try to implement the water-flow algorithm for line segmentation and measure accuracies with bounding polygon approach. 

<img width="211" alt="waterflow" src="https://user-images.githubusercontent.com/13045528/34940259-d6899526-fa14-11e7-9b6c-4b3a2aaa1a75.png">
