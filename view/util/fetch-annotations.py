#!/usr/bin/env python

from pyhocon import ConfigFactory
import json
import psycopg2
import psycopg2.extras
import sys

conf = ConfigFactory.parse_file('../view.conf')

annotations = conf.get_list('view.annotations')

def write_annotations():
    # write extractions to json file
    dbconf = conf.get('view.db.default')
    conn_string = "host='%s' dbname='%s' user='%s' password='%s'" % (
        dbconf.get('host'),
        dbconf.get('dbname'),
        dbconf.get('user'),
        dbconf.get('password'))
    conn = psycopg2.connect(conn_string)
    for ann in annotations:
        cursor = conn.cursor('ann_cursor', cursor_factory=psycopg2.extras.DictCursor)
        cursor.execute(ann.get('sql.query'))
        for row in cursor:
            print(row)


write_annotations()

#print(annotations)
