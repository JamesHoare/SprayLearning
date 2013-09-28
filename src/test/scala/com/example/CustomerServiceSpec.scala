package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.routing.HttpService
import concurrent.duration._


/**
 * specs2 testing
 */
class CustomerExampleSpec extends Specification with Specs2RouteTest with HttpService {


  def actorRefFactory = system // connect the DSL to the test ActorSystem

  val routeTestTimeout = RouteTestTimeout(2 second span)



  val customerRoute =
    get {
      path("getCustomer") {
        complete {
          <html>
            <body>
              <h1>Say hello to <i>James</i>!</h1>
            </body>
          </html>
        }
      } ~
        path("greeting") {
          complete("Hi!")
        }
    }

  "The service" should {

    "return a greeting for GET requests to the root path" in {
      Get("/getCustomer") ~> customerRoute ~> check {
        entityAs[String] must contain("Say hello")
      }
    }

    "return a 'Hi!' response for GET requests to /greeting" in {
      Get("/greeting") ~> customerRoute ~> check {
        entityAs[String] === "Hi!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/addCustomer") ~> customerRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(customerRoute) ~> check {
        status === MethodNotAllowed
        entityAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}