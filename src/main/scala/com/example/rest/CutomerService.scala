package com.example.rest

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import spray.routing.Directive.pimpApply
import spray.routing.directives.CompletionMagnet.fromObject
import spray.httpx.Json4sSupport
import org.json4s.{MappingException, Formats, DefaultFormats}
import com.example.domain.Customer
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


// case classes

case class ResponseError(errorCode: String, errorMessage: String) {}

case class SomeCustomException(msg: String) extends RuntimeException(msg) {}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class CustomerServiceActor extends Actor with CustomerService with AjaxService with CustomRejectionHandler {


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


  // set up cache
  lazy val customerCache = routeCache(maxCapacity = 1000, timeToLive = Duration("3 min"), timeToIdle = Duration("1 min"))




  //db service
  val customerService = new CustomerDAO

  //by specifying this, no need to explicitly add expected mediatype to each path
  //respondWithMediaType(MediaTypes.`application/json`)
  implicit def json4sFormats: Formats = DefaultFormats


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
    path("") {
      get {
        complete {
          throw new SomeCustomException("could not support your request")
        }
      }
    } ~
      path("customer") {
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
      path("customer" / LongNumber) {
        customerId =>
          get {
            cache(customerCache) {
              authenticate(authenticateUser) {
                user => {
                  complete {
                    log.debug("Retrieving customer with id %d".format(customerId))
                    customerService.get(customerId)
                  }
                }
              }
            }
          }
      } ~
      path("customergreeting" / Segment) {
        name =>
          get {
            complete("Welcome" + name)
          }
      } /*~
      path("orders") {
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
              }*/



}
