#!/usr/bin/env bash
cd "$(dirname "$0")"
. env.sh
set -eu

if [[ $(uname) = Darwin ]] &&
    osascript -e 'get path to application "iTerm"' &>/dev/null; then
    # On a Mac with iTerm.app, do something nice
    start() { ( source ./util/tab && tab "$@" ); }
else
    # otherwise, just run the process
    pids=()
    start() { local title=$1; shift; "$@" & pids+=($!); }
    trap wait EXIT
    trap 'kill -TERM "${pids[@]}"' HUP INT TERM
fi

# launch elasticsearch

start "ElasticSearch" elasticsearch

# launch nodejs
start "Nodejs" npm start

# launch react jsx watch
start "Reactjs" jsx --watch view/ public/js

