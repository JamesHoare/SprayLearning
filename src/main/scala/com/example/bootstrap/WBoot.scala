package com.example.bootstrap

import akka.actor.{ActorSystem, Props}
import spray.servlet.WebBoot
import com.example.rest.CustomerServiceActor

/**
 *
 * User: jameshoare
 * Date: 08/09/2013
 * Project: default-7846f0
 *
 */
object WBoot extends WebBoot {

  // we need an ActorSystem to host our application in
  val system = ActorSystem("example")

  // the service actor replies to incoming HttpRequests
  val serviceActor = system.actorOf(Props[CustomerServiceActor])

}
