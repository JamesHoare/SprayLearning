package com.example.search

import scalastic.elasticsearch.Indexer

/**
 *
 * Bootstrap ES
 *
 * User: jameshoare
 * Date: 07/09/2013
 *
 *
 */
trait SearchService {


  def setUpSearchService: Unit = {
    val indexType = "customer"

    val mapping = s"""
{
    "$indexType": {
        "properties" : {
            "firstName" : {"type": "String"},
            "lastName" : {"type": "String"},
            "category" : {"type": "String"}
        }
    }
}
"""
    val indexName = "customers"

    val indexer = Indexer.local.start

    indexer.client.admin().indices().prepareDelete(indexName).execute.actionGet

    indexer.createIndex(indexName, settings = Map("number_of_shards" -> "2"))
    indexer.waitTillActive()

    indexer.putMapping(indexName, indexType, mapping)

    indexer.index(indexName, indexType, "1", """{"firstName":"James", "lastName":"Hoare"}""")

    indexer.refresh()
  }


}
