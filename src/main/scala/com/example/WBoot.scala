package com.example

import akka.actor.{ActorSystem, Props}
import com.example.service.CustomerServiceActor
import spray.servlet.WebBoot

/**
 *
 * User: jameshoare
 * Date: 08/09/2013
 * Project: default-7846f0
 *
 */
class WBoot extends WebBoot {

  // we need an ActorSystem to host our application in
  val system = ActorSystem("example")

  // the service actor replies to incoming HttpRequests
  val serviceActor = system.actorOf(Props[CustomerServiceActor])

}
