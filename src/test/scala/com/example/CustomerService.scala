package com.example

import org.scalatest.{FreeSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import com.example.service.CustomerService
import org.json4s.{DefaultFormats, Formats}


/**
 *
 *
 * ScalaTest
 *
 * User: jameshoare
 * Date: 28/09/2013
 * Project: default-7846f0
 *
 */
class CustomerServiceTest extends FreeSpec with ScalatestRouteTest with Matchers with CustomerService {
  def actorRefFactory = system

  implicit def json4sFormats: Formats = DefaultFormats


  "The customer Service" - {
    "when calling GET getCustomerGreeting " - {
      "should return 'Hello James'" in {
        Get("/getCustomerGreeting") ~> customerRoutes ~> check {
          //status should equal(Ok)
          entityAs[String] should contain("Hello James")
        }
      }
    }
  }
}
