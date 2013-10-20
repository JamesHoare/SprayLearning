package com.example.domain

import akka.actor._
import scala.concurrent.duration._
import akka.util.Timeout
import com.example.mysql.CustomerDAO
import spray.http.StatusCodes
import akka.event.LoggingReceive
import com.example.rest.GetCustomerByID


/**
 *
 * User: jameshoare
 * Date: 14/10/2013
 * Project: default-7846f0
 *
 */
class CustomerServiceProxy extends Actor with ActorLogging
{


  val customerService = new CustomerDAO


  def receive  = LoggingReceive {

    case GetCustomerByID(customerId) => sender ! customerService.get(customerId)



  }
}
