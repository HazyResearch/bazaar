Bazaar
======

A collection of tools to generate input for DeepDive.

## [Parser](tree/master/parser)

Parser is a wrapper of Stanford CoreNLP which takes a simple JSON format as
input and generates a TSV file that can be directly loaded into a database. 

There are four different ways in which the parser package is used.

1. `parser/run.sh` runs the parser as a single process.
2. `parser/run_parallel.sh` runs multiple instances of the parser on a single machine.
3. [Distribute](tree/master/distribute) runs multiple instances of the parser on multiple machines.
4. [Condor](tree/master/condor) contains instructions on how th run the parser on the Condor cluster.
5. `parser/run.sh --` runs the parser as a REST service.

## [XML](http://github.com/hazyresearch/dd-genomics)

Many external datasets are in an XML format. To consume these datasets with DeepDive,
the XML has to be parsed into the simple JSON representation that the Parser package
uses as input.

An example of using an XML parser is contained in the dd-genomics project.

## [Distribute](tree/master/distribute)

It is often desirable to run the parser on multiple machines on ec-2 or azure. Distribute contains tools to automatically provision machines, distribute data, perform parsing, and collect results.

