#!/usr/bin/env bash

# exists
HEAD=$(curl -s -XHEAD -i 'http://localhost:9200/dd')
[ "${HEAD:0:15}" == "HTTP/1.1 200 OK" ] && EXISTS=1
if [ $EXISTS ]; then
  curl -XDELETE 'http://localhost:9200/dd/'
fi

curl -XPOST localhost:9200/dd -d '{
  "settings" : {
    "index" : {
      "number_of_shards" : 1
    },
    "analysis" : {
      "analyzer" : {
        "fulltext_analyzer" : {
          "type" : "custom",
          "tokenizer" : "whitespace",
          "filter" : [
            "lowercase"
          ]
        }
      }
    }
  },
  "mappings" : {
    "docs" : {
      "_source" : { "enabled" : true },
      "properties" : {
          "id" : {
            "type" : "string"
          },
          "content" : { 
            "type" : "string", 
            "term_vector" : "with_positions_offsets",
            "store" : false,
            "index_analyzer" : "fulltext_analyzer",
            "norms" : {
               "enabled" : false
            }
          },
          "extr1" : {
            "type" : "string",
            "index" : "not_analyzed"
          },
          "extr1_meta" : {
            "type" : "string",
            "index" : "not_analyzed"
          }
        }
      }
    }
  }'
