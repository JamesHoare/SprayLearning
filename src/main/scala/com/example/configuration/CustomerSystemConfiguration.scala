package com.example.configuration

import com.typesafe.config.ConfigFactory
import scala.util.Try
import akka.event.slf4j.SLF4JLogging

/**
 *
 * No Spring, so mixin system config via trait
 *
 * User: jameshoare
 * Date: 28/09/2013
 *
 *
 */
trait CustomerSystemConfiguration extends SLF4JLogging   {
  /**
   * Application config object.
   */
  val config = ConfigFactory.load()

  /** Host name/address to start service on. */
  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("127.0.0.1")

  /** Port to start service on. */
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)

  /** Database host name/address. */
  lazy val dbHost = Try(config.getString("db.host")).getOrElse("127.0.0.1")

  /** Database host port number. */
  lazy val dbPort = Try(config.getInt("db.port")).getOrElse(3306)

  /** Service database name. */
  lazy val dbName = Try(config.getString("db.name")).getOrElse("rest")

  /** Security auth. */
  lazy val configpassword = Try(config.getString("security.password")).getOrElse("Hoare")
  lazy val configusername = Try(config.getString("security.username")).getOrElse("James")

  /** User name used to access database. */
  lazy val dbUser = Try(config.getString("db.user")).toOption.orNull

  /** Password for specified user and database. */
  lazy val dbPassword = Try(config.getString("db.password")).getOrElse("password")
}
