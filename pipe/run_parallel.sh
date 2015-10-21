#!/bin/sh
# Parse sentences in parallel

# Example:
# ./run_parallel.sh --input=YOUR_INPUT.json --parallelism=1 '--formatIn json --formatOut column -v content -k doc_id -a ExtendedStanfordPipeline'


set -eu

# Usage: this_script input_file parallelism input_batch_size

if [ "$#" -le 1 ]; then
  echo "Usage: $0 -i=input_file [--parallelism=PARALLELISM] [--batch-size=BATCH_SIZE ] <args for run.sh>"
  exit
fi

for i in "$@"
do
case $i in
  -in=*|--input=*)
    INPUT_FILE="${i#*=}"
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
    *)
    echo "NO MATCH"
    break
    ;;
esac
done

if [ -z "$INPUT_FILE" ]; then
  echo "Usage: $0 -i=input_file [--parallelism=PARALLELISM] [--batch-size=BATCH_SIZE ] <args for run.sh>"
  exit
fi

PARALLELISM=${PARALLELISM:-2}
BATCH_SIZE=${BATCH_SIZE:-1000}

echo "parallelism = $PARALLELISM"
echo "batch-size  = $BATCH_SIZE"

RUN_SCRIPT=`cd $(dirname $0)/; pwd`"/run.sh $@"
echo $RUN_SCRIPT
mkdir -p $INPUT_FILE.split
rm -f $INPUT_FILE.split/*

# Split the input file into subfiles
split -a 10 -l $BATCH_SIZE $INPUT_FILE $INPUT_FILE.split/input-

# Match all files in the split directory
find $INPUT_FILE.split -name "input-*" 2>/dev/null -print0 | xargs -0 -P $PARALLELISM -L 1 bash -c "${RUN_SCRIPT}"' -i "$0" -o "$0.out"'

#echo "The output is is $INPUT_FILE.split"

#echo "If you generated TSV output, you can load it into your database using"
#echo "cat $INPUT_FILE.split/*.parsed | psql YOUR_DB_NAME -c "'"COPY sentences FROM STDIN"'

#echo "If you generated column output, you can merge the split columns using"
#echo ""

# merging column format segments

SPLITDIR=$INPUT_FILE.split
OUTDIR=$INPUT_FILE.out
if [ -d "$OUTDIR" ]; then
    echo "$OUTDIR already exists. Aborting."
    exit 1
fi
mkdir $OUTDIR


# first we determine the different annotators by looking at only one segment
annotations=()
for file in $SPLITDIR/*
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
for file in $SPLITDIR/*
do
    if [[ -d $file ]]; then
        for ann in "${annotations[@]}"
        do
            cat $file/ann.$ann >> $OUTDIR/ann.$ann
        done
    fi
done

# remove split dir
rm -rf $SPLITDIR

echo "The output is is $INPUT_FILE.out"
