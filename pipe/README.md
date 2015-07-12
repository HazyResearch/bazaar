Pipe
====

Lightweight schemas and processing framework for NLP. 

  In many DeepDive applications, errors in pre-processing become more relevant as one tries to push up precision and recall. Often no further quality improvement is possible without targeting these errors. 

  An example:
  ```
  $300. 00 per hour
  ```
  Our sentence splitter would break on the period and create two sentences.

  For some extractors we have tried work-arounds by adding complex rules to our extractors which target these errors. In fact, a significant portion of code in our 'rates' extractor is code to workaround this problem, but this code is complex and difficult to maintain.
  
  The right approach, of course, should be to fix the pre-processing components directly. Unfortunately, this is tricky because we treat all pre-processing as a black box, making changes nearly impossible. 

  Pipe solves this problem by breaking up the preprocessing components. It is now easy to add your custom tokenization or sentence splitting rules. For almost any domain, we want to add a few such domain-specific rules to improve pre-processing.
  
## Schemas





## Framework



## Setup

Run `setup.sh` to install dependencies and build the parser. Pipe requires Java 8.


[Plans](./plans.md)
