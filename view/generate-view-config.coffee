#!/usr/bin/env coffee
# A script that turns DDlog schema annotations into Bazaar/View configuration

fs = require "fs"
_  = require "underscore"

[schemaJsonFile, rest...] = process.argv[2..]

schema = JSON.parse fs.readFileSync schemaJsonFile

class RelationSchemaAnnotations
    constructor: (@name, @relation) ->

    findAll: (props) =>     _.where @relation.annotations, props
    find:    (props) => _.findWhere @relation.annotations, props
    has:     (props) => @find props

    satisfies: (schemaObj, annotationCond, schemaCond) =>
        (not schemaCond? or (_.findWhere [schemaObj], schemaCond)) and
        (not annotationCond? or (_.findWhere schemaObj?.annotations, annotationCond))

    columnNamesWith: (cond...) =>
        for name, column of @relation.columns when @satisfies column, cond...
            name
    columnsWith: (cond...) =>
        for name, column of @relation.columns when @satisfies column, cond...
            column

    columnNameWith: (cond...) =>
        for name, column of @relation.columns when @satisfies column, cond...
            return name
    columnWith: (cond...) =>
        for name, column of @relation.columns when @satisfies column, cond...
            return column

console.log """
    view {

    docs: {
        # TODO
    }

    annotations: [
    """

for relName,relSchema of schema.relations when relSchema.annotations
    r = new RelationSchemaAnnotations relName, relSchema
    if r.has {name: "textspan"}
        relName = r.name
        colStart = r.columnNameWith {name: "textspan_start"}, {type: "int"}
        colLength = r.columnNameWith {name: "textspan_length"}, {type: "int"}

        console.log """
   {     
     name: #{relName}
     input: data/annotations.#{relName}
     sql: {
       query: "
           SELECT mention_id, sentence_id, #{colStart}, #{colStart} + #{colLength}, text
           FROM #{relName}
       "
     }
   } 
        """

console.log """
    ]
    }
    """
