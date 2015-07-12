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



ann.text
```
"This is a very simple text file.\nIt contains two sentences."
```

ann.poss
```
["DT","VBZ","DT","RB","JJ","NN","NN",".","PRP","VBZ","CD","NNS","."]
```

ann.tokens
```
["This","is","a","very","simple","text","file",".","It","contains","two","sentences","."]
```
ann.tokenOffsets
```
[[0,4],[5,7],[8,9],[10,14],[15,21],[22,26],[27,31],[31,32],[33,35],[36,44],[45,48],[49,58],[58,59]]
```

ann.sentenceOffsets
```
[[0,32],[33,59]]
```

ann.sentenceTokenOffsets
```
[[0,8],[8,13]]
```




## Framework

* Use scala static typing or not
* Use python


## Setup

Run `setup.sh` to install dependencies and build the parser. Pipe requires Java 8.


[Plans](./plans.md)
