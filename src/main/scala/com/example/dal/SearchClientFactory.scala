package com.example.dal

import io.searchbox.client.config.ClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import spray.routing.HttpService

/**
 *
 *
 * Setup the search client
 *
 * User: jameshoare
 * Date: 31/08/2013
 *
 *
 */
trait SearchClientFactory extends HttpService {


  def getSearchClient: JestClient = {
    val connectionUrl = "http://localhost:9200"

    // Configuration
    val clientConfig = new ClientConfig.Builder(connectionUrl).multiThreaded(true).build()

    // Construct a new Jest client according to configuration via factory
    val factory = new JestClientFactory();
    factory.setClientConfig(clientConfig);
    factory.getObject();
  }


}
