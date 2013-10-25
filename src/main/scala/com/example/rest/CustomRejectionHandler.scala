package com.example.rest

import spray.routing._
import spray.http._
import StatusCodes._

trait CustomRejectionHandler extends HttpService {
  implicit val myRejectionHandler = RejectionHandler {
    case AuthenticationFailedRejection(credentials,List()) :: _ => complete(Unauthorized, "Credential fail " + credentials)
    case _ => complete(BadRequest, "Something went wrong here")
  }
}