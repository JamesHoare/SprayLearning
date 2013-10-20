package com.example.domain

import akka.actor._
import scala.concurrent.duration._
import akka.util.Timeout
import com.example.mysql.CustomerDAO
import spray.http.StatusCodes
import akka.event.LoggingReceive


/**
 *
 * User: jameshoare
 * Date: 14/10/2013
 * Project: default-7846f0
 *
 */
class CustomerActor extends Actor with ActorLogging
{

  //implicit val timeout = Timeout(5 seconds)
  val customerService = new CustomerDAO
  //val customer = None


  def receive  = LoggingReceive {

    case "String" => sender ! customerService.get(1)



  }
}
