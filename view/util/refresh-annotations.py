#!/usr/bin/env python

ES_HOST = {"host" : "localhost", "port" : 9200}
INDEX_NAME = 'view'
TYPE_EXTRACTORS_NAME = 'extractors'
TYPE_EXTRACTIONS_NAME = 'extractions'

from pyhocon import ConfigFactory
from elasticsearch import Elasticsearch
import json
import psycopg2
import psycopg2.extras
import sys

conf = ConfigFactory.parse_file('../view.conf')

annotations = conf.get_list('view.annotations')

es = Elasticsearch(hosts = [ES_HOST])

def index_extractors():
  es.delete_by_query(index = INDEX_NAME, doc_type = TYPE_EXTRACTORS_NAME, body = {
      "query": {
        "match_all": {}
      }
  })
  for ann in annotations:
    es.index(index = INDEX_NAME, doc_type = TYPE_EXTRACTORS_NAME, body = {
      "name" : ann.get('name')
    }, refresh = False)
  es.indices.refresh(index = INDEX_NAME)

def index_extractions():
  es.delete_by_query(index = INDEX_NAME, doc_type = TYPE_EXTRACTIONS_NAME, body = {
      "query": {
        "match_all": {}
      }
  })
  dbconf = conf.get('view.database.default')
  conn_string = "host='%s' dbname='%s' user='%s' password='%s'" % (
    dbconf.get('host'),
    dbconf.get('dbname'),
    dbconf.get('user'),
    dbconf.get('password'))
  conn = psycopg2.connect(conn_string)
  for ann in annotations:
	cursor = conn.cursor('ann_cursor', cursor_factory=psycopg2.extras.DictCursor)
	cursor.execute('SELECT * FROM %s' % (ann.get('table')))
	row_count = 0
	for row in cursor:
		row_count += 1
		print "row: %s    %s\n" % (row_count, row)

        #es.index(index = INDEX_NAME, doc_type = TYPE_EXTRACTIONS_NAME, body = {
        #   "name" : "genepheno"
        # }, refresh = False)
        #  es.indices.refresh(index = INDEX_NAME)

index_extractors()
index_extractions()

print(annotations)
