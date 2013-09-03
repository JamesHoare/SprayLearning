package com.example.service

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.routing.Directive.pimpApply
import spray.routing.directives.CompletionMagnet.fromObject
import spray.httpx.Json4sSupport
import org.json4s.Formats
import org.json4s.DefaultFormats
import com.example.model.Customer
import org.json4s.JsonAST.JObject
import com.example.dal.{SearchClientFactory, CustomerDal}
import shapeless._
import spray.routing.directives.BasicDirectives._
import spray.util.LoggingContext
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import io.searchbox.client.JestClientFactory
import io.searchbox.core.{Index, Bulk}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class CustomerServiceActor extends Actor  with CustomerService with AjaxService with CustomRejectionHandler  {

  implicit def json4sFormats: Formats = DefaultFormats

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = handleTimeouts orElse runRoute(handleRejections(myRejectionHandler)(handleExceptions(myExceptionHandler)(
    customerRoutes ~ ajaxRoutes)))

  def handleTimeouts: Receive = {
    case Timedout(x: HttpRequest) =>
      sender ! HttpResponse(StatusCodes.InternalServerError, "Something is taking way too long.")
  }

  implicit def myExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler.apply {
      case e: SomeCustomException => ctx => {
        log.debug("%s %n%s %n%s".format(e.getMessage, e.getStackTraceString, e.getCause))
        ctx.complete(404, e.getMessage)
      }
      case e: Exception => ctx => {
        log.debug("%s %n%s %n%s".format(e.getMessage, e.getStackTraceString, e.getCause))
        ctx.complete(500, e.getMessage)
      }
    }
}

class SomeCustomException(msg: String) extends RuntimeException(msg)

//http://kufli.blogspot.com/2013/08/sprayio-rest-service-api-versioning.html
trait VersionDirectives {
  def versioning: Directive[String :: HNil] =
    extract {
      ctx =>
        val header = ctx.request.headers.find(_.name == "X-API-Version")
        header match {
          case Some(head) => head.value
          case _ => "1" //default to 1
        }
    }
}

trait AjaxService extends HttpService {
  val ajaxRoutes =
    path("search" / Segment) {
      query =>
        get {
          complete {
            //Free text search implementation
            s"success ${query}"
          }
        }
    }
}

// this trait defines our service behavior independently from the service actor
trait CustomerService extends HttpService with Json4sSupport with UserAuthentication with SearchClientFactory  {

  //http://kufli.blogspot.com/2013/08/sprayio-rest-service-api-versioning.html
  val Version = PathMatcher( """v([0-9]+)""".r)
    .flatMap {
    case vString :: HNil => {
      try Some(Integer.parseInt(vString) :: HNil)
      catch {
        case _: NumberFormatException => Some(1 :: HNil) //default to version 1
      }
    }
  }

  val customerRoutes =
    path("someException") {
      get {
        complete {
          throw new SomeCustomException("This is a custom Exception James")
        }
      }
    } ~
      path("addCustomer") {
        post {
          authenticate(authenticateUser) {
            user =>
              entity(as[JObject]) {
                customerObj =>
                  complete {
                    val customer = customerObj.extract[Customer]
                    val customerDal = new CustomerDal
                    val id = customerDal.saveCustomer(customer)
                    id.toString()
                  }
              }
          }
        }
      } ~
      path("getCustomer" / Segment) {
        customerId =>
          get {
            authenticate(authenticateUser) {
              user => {
                complete {
                  //get customer from db using customerId as Key
                  val customerDal = new CustomerDal
                  val customer = customerDal.findCustomer(customerId)
                  customer
                }
              }
            }
          }
      } ~
      path("getCust" / Segment) {
        customerId =>
          get {
            authenticate(authenticateUser) {
              user => {
                complete {
                  val customer = Customer(firstName = "James",
                    lastName = "Hoare", _id = Some(customerId))
                  // get the search client
                  val bulk = new Bulk.Builder()
                    .defaultIndex("customers")
                    .defaultType("customer")
                    .addAction(new Index.Builder(customer).build())
                    .build()

                    getSearchClient.execute(bulk);

                  customer //return customer obj
                }
              }
            }
          }
      }
}
