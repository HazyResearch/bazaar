#!/bin/bash
DESTDIR=./lib
mkdir -p $DESTDIR
wget -P $DESTDIR http://nlp.stanford.edu/software/stanford-srparser-2014-10-23-models.jar
