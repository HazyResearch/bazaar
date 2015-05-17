#!/bin/sh
# Parse sentences in parallel

set -eu

# Usage: this_script input_file parallelism input_batch_size

if [ "$#" -le 1 ]; then
  echo "Usage: $0 input_file parallelism [input_batch_size=1000] [sentence_words_limit=120]"
  exit
fi

INPUT_FILE=$1
PARALLELISM=$2
export INPUT_BATCH_SIZE=${3:-1000}
export SENTENCE_WORDS_LIMIT=${4:-120}

RUN_SCRIPT=`cd $(dirname $0)/; pwd`/run.sh
echo $RUN_SCRIPT
mkdir -p $INPUT_FILE.split
rm -f $INPUT_FILE.split/*

# Split the input file into subfiles
split -a 10 -l $INPUT_BATCH_SIZE $INPUT_FILE $INPUT_FILE.split/input-

# Match all files in the split directory
find $INPUT_FILE.split -name "input-*" 2>/dev/null -print0 | xargs -0 -P $PARALLELISM -L 1 bash -c "${RUN_SCRIPT} -l ${SENTENCE_WORDS_LIMIT}"' -f "$0"'

echo "Output TSV files are in: $INPUT_FILE.split/*.parsed"
echo "To load them into the databse, run: cat $INPUT_FILE.split/*.parsed | psql YOUR_DB_NAME -c "'"COPY sentences FROM STDIN"'
