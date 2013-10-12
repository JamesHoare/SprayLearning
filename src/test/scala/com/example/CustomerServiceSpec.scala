package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import spray.routing.{AuthenticationFailedRejection, Directives, HttpService}
import concurrent.duration._
import com.example.rest.{CustomerServiceActor, CustomerService}
import com.example.domain.Customer
import spray.http.HttpHeaders.Date


/**
 * specs2 testing
 */
class CustomerExampleSpec extends Specification with Specs2RouteTest with CustomerService {


  def actorRefFactory = system // connect the DSL to the test ActorSystem


  "The service" should {
    "return a greeting for GET requests to the customergreeting path" in {
      Get("/customergreeting?name=James") ~> customerRoutes ~> check {
        //entityAs[String] === "Welcome 'James'"
        status.toString() === "200 OK"
      }
    }


    "return a customer for GET requests to the customer path" in {
      Get("/customer/9?usr=James&pwd=Hoare") ~> customerRoutes ~> check {
        status.toString() === "200 OK"
        entityAs[Customer] === Customer(Some(9), "Jess", "Hoare", None)
      }
    }


    "reject a request for GET customer without specifying credentials" in {
      Get("/customer/1") ~> sealRoute(customerRoutes) ~> check {
        status.toString === "500 Internal Server Error"

      }
    }


    "create a new customer" in {
      Post("customer", """{"firstName":
      "Jess", "lastName":"Hoare", "birthday":"2007-06-29T18:06:22Z"}""") ~> customerRoutes ~> check {
        handled === true

      }
    }


  }
}