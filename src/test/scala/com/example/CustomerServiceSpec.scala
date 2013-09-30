package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.routing.{Directives, HttpService}
import concurrent.duration._
import com.example.rest.{CustomerServiceActor, CustomerService}


/**
 * specs2 testing
 */
class CustomerExampleSpec extends Specification  with Specs2RouteTest with CustomerService {


  def actorRefFactory = system // connect the DSL to the test ActorSystem


  "The service" should {
    "return a greeting for GET requests to the customerGreeting path" in {
      Get("/customerGreeting") ~> customerRoutes ~> check {
        //entityAs[String] must contain("Hello James")
        status.toString() === "200 OK"
      }
    }


      "return a customer for GET requests to the customer path" in {
        Get("/customer/1?usr=James&pwd=Hoare") ~> customerRoutes ~> check {
          status.toString() === "200 OK"
        }
      }


  }
}