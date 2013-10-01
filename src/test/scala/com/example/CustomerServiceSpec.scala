package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.routing.{AuthenticationFailedRejection, Directives, HttpService}
import concurrent.duration._
import com.example.rest.{CustomerServiceActor, CustomerService}


/**
 * specs2 testing
 */
class CustomerExampleSpec extends Specification  with Specs2RouteTest with CustomerService {


  def actorRefFactory = system // connect the DSL to the test ActorSystem


  "The service" should {
    "return a greeting for GET requests to the customergreeting path" in {
      Get("/customergreeting/James") ~> customerRoutes ~> check {
        //entityAs[String] must contain("Hello James")
        status.toString() === "200 OK"
      }
    }


      "return a customer for GET requests to the customer path" in {
        Get("/customer/1?usr=James&pwd=Hoare") ~> customerRoutes ~> check {
          status.toString() === "200 OK"
        }
      }

    "reject a request for GET customer without specifying credentials" in {
      Get("/customer/1") ~> sealRoute(customerRoutes) ~> check {
        status.toString === "500 Internal Server Error"

      }
    }

    /*reject requests with a Basic Authentication header" in {
      Get("/api/frontend/incidents") ~> addHeader(Authorization(BasicHttpCredentials("user", "password"))) ~>
      frontendApiRoutes ~>
      check {
      handled must beFalse

      rejection must beAnInstanceOf[AuthenticationFailedRejection]
      }
      }*/
  }
}