package com.example.service

import spray.routing.authentication.Authentication
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing.AuthenticationFailedRejection
import com.example.configuration.CustomerSystemConfiguration
import com.mysql.jdbc.log.Slf4JLogger
import akka.event.slf4j.SLF4JLogging

case class User(userName: String, token: String) {}

trait UserAuthentication extends CustomerSystemConfiguration with SLF4JLogging {


  def authenticateUser: ContextAuthenticator[User] = {
    ctx =>
      {
        //get username and password from the url        
        val usr = ctx.request.uri.query.get("usr").get
        val pwd = ctx.request.uri.query.get("pwd").get
        log.debug("usr..." + usr + "pwd..." + pwd)

        doAuth(usr, pwd)
      }
  }

  private def doAuth(userName: String, password: String): Future[Authentication[User]] = {
    //here you can call database or a web service to authenticate the user    
    Future {
      Either.cond(password == configpassword && userName == configusername,
        User(userName = userName, token = java.util.UUID.randomUUID.toString),
        AuthenticationFailedRejection("CredentialsRejected"))
    }
  }
}