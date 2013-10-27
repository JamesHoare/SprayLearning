package com.example.service

import spray.routing.authentication._
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing.AuthenticationFailedRejection
import com.example.configuration.CustomerSystemConfiguration
import com.mysql.jdbc.log.Slf4JLogger
import akka.event.slf4j.SLF4JLogging
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import scala.Some
import spray.routing.authentication.UserPass


trait UserAuthentication extends CustomerSystemConfiguration with SLF4JLogging {


  def myUserPassAuthenticator(userPass: Option[UserPass]): Future[Option[String]] =
    Future {
      if (userPass.exists(up => up.user == configusername && up.pass == configpassword)) Some("John")
      else None
    }





}