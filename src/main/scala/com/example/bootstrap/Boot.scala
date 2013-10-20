package com.example.bootstrap

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.example.search.SearchService
import com.example.configuration.CustomerSystemConfiguration
import com.example.rest.CustomerServiceActor
import com.example.domain.CustomerServiceProxy

/**
 * Bootstrap spray can
 */
object Boot extends App with SearchService with CustomerSystemConfiguration {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val restService = system.actorOf(Props[CustomerServiceActor], "customer-service")



  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(restService, serviceHost, servicePort)

  setUpSearchService


  /* Allow a user to shutdown the service easily */
  readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()






}