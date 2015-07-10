var express = require('express');
var router = express.Router();

var elasticsearch = require('elasticsearch');
var client = new elasticsearch.Client({
  host: 'http://localhost:9200'
});

router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express3' });
});

router.get('/extractors', function(req, res, next) {
  client.search({
    index: process.env.INDEX_NAME,
    type: 'extractors',
    body: {
      query: {
        'match_all': {}
      }
    }
  }).then(function(body) {
    var hits = body.hits.hits;
    res.send(hits);
  }, function (err) {
    console.trace(err.message);
    next(err);
  });
});

router.get('/docs', function(req, res, next) {
  var from = req.param('from', 0)
  var limit = req.param('limit', 100)
  var keywords = req.query.keywords
  var facets = req.query.facets

  var obj = {
    index: process.env.INDEX_NAME, 
    type: 'docs',
    from: from,
    size: limit,
    body: {
      query: {
        "match_all" : {}
      },
      highlight : {
        fields : {
          content : { "number_of_fragments" : 0 }
        }
      }
    }
  }

  if (keywords.length > 0) {
    obj.body.query = {
        query_string: {
          "default_field" : "content",
          "fields" : ["content", "_id", "id"],
          "query" : keywords
        }
      }
  }

  if (facets.length > 0) {
    var l = facets.split(',')

    var filters = []
    for (var i=0; i < l.length; i++)
      filters.push({
        "exists" : { "field" : l[i] }
      });

    if (filters.length > 1)
      obj.body.filter = {
        "and" : filters
      }
    else
      obj.body.filter = filters[0]
  }

  client.search(
   obj
  ).then(function (body) {
    var hits = body.hits.hits;
    res.send(hits)
  }, function (err) {
    console.trace(err.message);
    next(err)
  });
});


module.exports = router;
