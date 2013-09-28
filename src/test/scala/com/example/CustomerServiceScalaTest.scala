package com.example

import org.scalatest.{FreeSpec, Matchers}
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService



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
class CustomerServiceTest extends FreeSpec with ScalatestRouteTest with Matchers with HttpService {

  def actorRefFactory = system


  val customerRoute =
    get {
      path("getCustomerGreeting") {
        complete {
          "Hello James"
        }
      }
    }


  "The customer Service" - {
    "when calling GET getCustomerGreeting " - {
      "should return 'Hello James'" in {
        Get("/getCustomerGreeting") ~> customerRoute ~> check {
          //status should equal(Ok)
          entityAs[String] === "Hello James"
        }
      }
    }
  }
}
