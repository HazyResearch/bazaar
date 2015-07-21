#!/usr/bin/env bash

curl -XGET 'http://localhost:9200/view/docs/_search' -d '{
      query: {
        function_score: {
          query: {
            "match_all" : {}
          },
          "random_score" : { "seed" : 1376773391128418000 }
        }
      }      
    }'


