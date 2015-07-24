#!/usr/bin/env bash
cd "$(dirname "$0")"
. env.sh
set -eu

cd util
./fetch-sentences-table.py
./refresh-documents.py
