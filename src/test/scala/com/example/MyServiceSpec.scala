package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.routing.HttpService

class CustomerExampleSpec extends Specification with Specs2RouteTest with HttpService {
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  val smallRoute =
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
      Get("/getCustomer") ~> smallRoute ~> check {
        entityAs[String] must contain("Say hello")
      }
    }

    "return a 'Hi!' response for GET requests to /greeting" in {
      Get("/greeting") ~> smallRoute ~> check {
        entityAs[String] === "Hi!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/addCustomer") ~> smallRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(smallRoute) ~> check {
        status === MethodNotAllowed
        entityAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}