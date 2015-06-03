#!/bin/bash

DIRNAME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# fetch SR models
DESTDIR=$DIRNAME/lib
FILENAME='stanford-srparser-2014-10-23-models.jar'
if [ ! -e "$DESTDIR/$FILENAME" ]; then
    mkdir -p $DESTDIR
    wget -P $DESTDIR http://nlp.stanford.edu/software/stanford-srparser-2014-10-23-models.jar
else
    echo "Skipping download: $DESTDIR/$FILENAME already exists"
fi

# build parser
cd $DIRNAME
sbt/sbt stage
