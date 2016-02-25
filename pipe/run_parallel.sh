#!/usr/bin/env bash

# Parses documents in parallel.
#
# Input is a single file that contains one JSON record per line.
# Output is a single file that contains one JSON record per line.
#
# The number of records and their order is the same in input and output.
#
# Example:
# ./run_parallel.sh --input=INPUT.json --output=OUTPUT.json \
#                   --params='-v content -k doc_id -a ExtendedCleanHtmlStanfordPipeline'
#
# The following environment variables are used when available.
#   PARALLELISM (default 2)
#   BATCH_SIZE  (default 1000)

set -eu

for i in "$@"
do
case $i in
  -in=*|--input=*)
    INPUT_FILE="${i#*=}"
    shift
    ;;
  -out=*|--output=*)
    OUTPUT_FILE="${i#*=}"
    shift
    ;;
  -pa=*|--params=*)
    PARAMS="${i#*=}"
    shift
    ;;
  -p=*|--parallelism=*)
    PARALLELISM="${i#*=}"
    shift
    ;;
  -b=*|--batch-size=*)
    BATCH_SIZE="${i#*=}"
    shift
    ;;
  --keepsplit)
    KEEP_SPLIT=true
    shift
    ;;
  --skip-split)
    SKIP_SPLIT=true
    KEEP_SPLIT=true # do not touch the original input
    shift
    ;;
  --skip-merge)
    SKIP_MERGE=true
    KEEP_SPLIT=true # if output data is not merged, should keep the split data
    shift
    ;;
  --compress)
    COMPRESS=true
    shift
    ;;
    *)
    echo "Ignoring parameter: $i"
    break
    ;;
esac
done

if [ -z "$INPUT_FILE" ]; then
  echo "Usage: $0 -in=INPUT.json [-out=OUTPUT.json] [--parallelism=PARALLELISM] \\"
  echo "                    [--batch-size=BATCH_SIZE ] --params='<args for run.sh>'"
  exit
fi

# Setting defaults
PARALLELISM=${PARALLELISM:-2}
BATCH_SIZE=${BATCH_SIZE:-1000}
PARAMS=${PARAMS:-}
KEEP_SPLIT=${KEEP_SPLIT:-false}
COMPRESS=${COMPRESS:-false}
if [ "$COMPRESS" = false ]; then
    OUTPUT_FILE=${OUTPUT_FILE:-$INPUT_FILE.out}
else
    OUTPUT_FILE=${OUTPUT_FILE:-$INPUT_FILE.out.gz}
fi

echo "parallelism = $PARALLELISM"
echo "batch-size  = $BATCH_SIZE"
echo "compress    = $COMPRESS"

# Fixed a bug when "config.properties" does not exists
touch config.properties

export RUN_SCRIPT=`cd $(dirname $0)/; pwd`"/run.sh --formatIn json --formatOut json $PARAMS"
echo $RUN_SCRIPT

SPLIT_DIR=$INPUT_FILE.split
mkdir -p $SPLIT_DIR
rm -rf $SPLIT_DIR/*

# Split the input file into subfiles
if [ -z "${SKIP_SPLIT:-}" ]; then
    if [ "$COMPRESS" = false ]; then
        split -n l/$PARALLELISM $INPUT_FILE $SPLIT_DIR/input-
    else
        echo "Error: do not support splitting data from gzipped format yet. Pre-split the data yourself and specify --skip-split."
        exit 1
    fi
else
  # if skip-split is specified, use the input as a directory that contains pre-split data
  SPLIT_DIR=$INPUT_FILE
fi

# Match all files in the split directory
if [ "$COMPRESS" = false ]; then
    find $SPLIT_DIR/ -type f 2>/dev/null -print0 | xargs -0 -P $PARALLELISM -L 1 bash -c "${RUN_SCRIPT}"' -i "$0" -o "$0.out"'
else
    echo "Running with compression: read gzipped data from $SPLIT_DIR/, write to $SPLIT_DIR/*.out"

    find $SPLIT_DIR/ -type f -name "*.gz" 2>/dev/null -print0 | xargs -0 -P $PARALLELISM -L 1 bash -c 'gunzip --stdout "$0" | `echo "${RUN_SCRIPT}"` -i /dev/stdin -o "$0.out" -c true'
fi

function merge_json_format {
    SPLIT_DIR=$1
    OUTPUT_FILE=$2
    # merging json files
    for file in $SPLIT_DIR/*.out
    do
        cat $file >> $OUTPUT_FILE
    done
}


function merge_column_format {
    SPLIT_DIR=$1
    OUTPUT_FILE=$2
    # merging column format segments

    OUTDIR=$INPUT_FILE.out
    if [ -d "$OUTDIR" ]; then
        echo "$OUTDIR already exists. Aborting."
        exit 1
    fi
    mkdir $OUTDIR

    # first we determine the different annotators by looking at only one segment
    annotations=()
    for file in $SPLIT_DIR/*
    do
        if [[ -d $file ]]; then
            for ann in $file/*
            do
                annotations+=("${ann##*.}")
            done
            break
        fi
    done

    # now cat them all together
    for file in $SPLIT_DIR/*
    do
        if [[ -d $file ]]; then
            for ann in "${annotations[@]}"
            do
                cat $file/ann.$ann >> $OUTDIR/ann.$ann
            done
        fi
    done
}

if [ -z "${SKIP_MERGE:-}" ]; then
    merge_json_format $SPLIT_DIR $OUTPUT_FILE
    echo "The output is in $OUTPUT_FILE"
else
    echo "The output is in $SPLIT_DIR/*.out"
fi

# remove split dir
if [ "$KEEP_SPLIT" = false ]; then
    rm -rf $SPLIT_DIR
fi
