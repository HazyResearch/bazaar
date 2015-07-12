#!/usr/bin/env bash

# launch elasticsearch
sudo bash -c "source ./util/tab && tab ElasticSearch util/elasticsearch-1.6.0/bin/elasticsearch "

# launch nodejs
sudo bash -c "source ./util/tab && tab Nodejs npm start "

# launch react jsx watch
sudo bash -c "source ./util/tab && tab Reactjs jsx --watch view/ public/js"

