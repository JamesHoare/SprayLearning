package com.example.rest

import akka.actor.{Props, ActorRef, Actor}
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import spray.routing.Directive.pimpApply
import spray.httpx.Json4sSupport
import org.json4s.{MappingException, Formats, DefaultFormats}
import com.example.domain._
import org.json4s.JsonAST.JObject
import com.example.dal.CustomerDal
import scala.concurrent.ExecutionContext.Implicits.global
import shapeless._
import spray.routing.directives.BasicDirectives._
import spray.util.LoggingContext
import com.example.service.UserAuthentication
import akka.event.slf4j.SLF4JLogging
import com.example.mysql.CustomerDAO
import spray.caching.{LruCache}
import scala.concurrent.duration.{Duration}
import spray.routing.directives.CachingDirectives
import spray.can.server.Stats
import spray.can.Http
import spray.httpx.marshalling.Marshaller
import spray.httpx.encoding.Gzip
import spray.util._
import spray.http._
import MediaTypes._
import CachingDirectives._
import akka.util.Timeout
import spray.httpx.unmarshalling._
import spray.http.HttpRequest
import shapeless.::
import scala.Some
import com.example.rest.SomeCustomException
import spray.http.HttpResponse
import com.example.rest.ResponseError
import spray.http.Timedout
import org.json4s.MappingException
import java.util.Date
import java.text.{ParseException, SimpleDateFormat}
import spray.http.HttpRequest
import shapeless.::
import scala.Some
import com.example.rest.SomeCustomException
import spray.http.HttpResponse
import com.example.rest.ResponseError
import spray.http.Timedout
import org.json4s.MappingException
import scala.xml.XML
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import spray.http.HttpRequest
import com.example.domain.GetCustomerByID
import com.example.domain.Failure
import shapeless.::
import scala.Some
import com.example.rest.SomeCustomException
import spray.http.HttpResponse
import com.example.rest.ResponseError
import com.example.domain.Customer
import spray.http.Timedout
import com.example.rest.Order
import org.json4s.MappingException


// case classes

case class ResponseError(errorCode: String, errorMessage: String) {}

case class SomeCustomException(msg: String) extends RuntimeException(msg) {}

case class Order(customerId: String, productId: String, number: Int) {}

case class OrderId(id: Long) {}

case class TrackingOrder(id: Long, status: String, order: Order) {}

case class NoSuchOrder(id: Long) {}


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class CustomerServiceActor extends Actor with CustomerService with AjaxService with CustomRejectionHandler {


  /* //by specifying this, no need to explicitly add expected mediatype to each path
   //respondWithMediaType(MediaTypes.`application/json`)
   implicit def json4sFormats: Formats = DefaultFormats*/

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  def receive = handleTimeouts orElse runRoute(handleRejections(myRejectionHandler)(handleExceptions(myExceptionHandler)(
    customerRoutes ~ ajaxRoutes)))


  def handleTimeouts: Receive = {
    case Timedout(x: HttpRequest) =>
      sender ! HttpResponse(StatusCodes.InternalServerError, "Something is taking way too long.")
  }

  implicit def myExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler.apply {
      case m: MappingException => {
        respondWithMediaType(`application/json`) {
          val errorMsg = ResponseError("MalformedBody", m.getMessage)
          ctx => ctx.complete(415, errorMsg)
        }
      }
      case e: SomeCustomException => ctx => {
        val errorMsg = ResponseError("BadRequest", e.getMessage)
        ctx.complete(400, errorMsg)
      }
      case e: Exception => ctx => {
        val errorMsg = ResponseError("InternalServerError", e.getMessage)
        ctx.complete(500, errorMsg)
      }
    }


}


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


//decouple route logic from Actor/business work
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

//decouple route logic from Actor/business work
trait CustomerService extends HttpService with Json4sSupport with UserAuthentication with SLF4JLogging {

  implicit def json4sFormats: Formats = DefaultFormats

  // we use the enclosing ActorContext's or ActorSystem's dispatcher for our Futures and Scheduler
  implicit def executionContext = actorRefFactory.dispatcher


  implicit val timeout = Timeout(5 seconds)

  import akka.pattern._


  // set up cache
  lazy val customerCache = routeCache(maxCapacity = 1000, timeToLive = Duration("3 min"), timeToIdle = Duration("1 min"))


  //db service
  val customerService = new CustomerDAO


  val customerServiceProxy = actorRefFactory.actorOf(Props[CustomerServiceProxy], "customer-service-actor")


  /* val Version = PathMatcher( """v([0-9]+)""".r)
     .flatMap {
     case vString :: HNil => {
       try Some(Integer.parseInt(vString) :: HNil)
       catch {
         case _: NumberFormatException => Some(1 :: HNil) //default to version 1
       }
     }
   }*/

  val customerRoutes =
    path("customer") {
      post {
        //authenticate(authenticateUser) {
          //user =>
            entity(as[JObject]) {
              customerObj =>
                complete {
                  val customer = customerObj.extract[Customer]
                  log.debug(s"Creating customer: %s".format(customer))
                  (customerServiceProxy ? CreateCustomer(customer)).mapTo[Either[Customer.type, Failure.type]]
                }
            }
        //}
      }
    } ~
      path("customer" / LongNumber) {
        customerId =>
          get {
            cache(customerCache) {
              authenticate(authenticateUser) {
                user => {
                  complete {
                    log.debug(s"Retrieving customer with id:  $customerId")
                    (customerServiceProxy ? GetCustomerByID(customerId)).mapTo[Either[Customer.type, Failure.type]]
                  }
                }
              }
            }
          }
      } ~
      path("customergreeting") {
        get {
          parameter('name) {
            name =>
              complete(s"Welcome '$name'")
          }
        }
      }
  /*~ path("orders") {
       get {
         parameters('id.as[Long]).as(OrderId) {
           orderId =>
           //get status
             complete {
               val askFuture = orderSystem ? orderId
               askFuture.map {
                 case result: TrackingOrder => {
                   <statusResponse>
                     <id>
                       {result.id}
                     </id>
                     <status>
                       {result.status}
                     </status>
                   </statusResponse>
                 }
                 case result: NoSuchOrder => {
                   <statusResponse>
                     <id>
                       {result.id}
                     </id>
                     <status>ID is unknown</status>
                   </statusResponse>
                 }
               }
             }
         }
       }
     }*/
}

object XMLConverter {
  def createOrder(content: String): Order = {
    val xml = XML.loadString(content)
    val order = xml \\ "order"
    val customer = (order \\ "customerId").text
    val productId = (order \\ "productId").text
    val number = (order \\ "number").text.toInt
    new Order(customer, productId, number)
  }
}



