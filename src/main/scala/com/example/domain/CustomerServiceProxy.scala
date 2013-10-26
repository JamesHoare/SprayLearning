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
 *
 *
 */
class CustomerServiceProxy extends Actor with CustomerDAO
{




  def receive  = LoggingReceive {

    case GetCustomerByID(customerId) => sender ! get(customerId)

    case CreateCustomer(customer) => sender ! create(customer)

  }
}
