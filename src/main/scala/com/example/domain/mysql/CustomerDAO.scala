package com.example.mysql

import com.example.configuration.CustomerSystemConfiguration
import scala.slick.jdbc.meta.MTable
import scala.slick.session.Database
import com.example.domain.Customers
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.driver.MySQLDriver.simple._

/**
 *
 *
 * slick integration
 *
 * User: jameshoare
 * Date: 28/09/2013
 *
 *
 */
class CustomerDAO extends CustomerSystemConfiguration {

  // init Database instance
  private val db = Database.forURL(url = "jdbc:mysql://%s:%d/%s".format(dbHost, dbPort, dbName),
    user = dbUser, password = dbPassword, driver = "com.mysql.jdbc.Driver")

  // create tables if not exist
  db.withSession {
    if (MTable.getTables("customers").list().isEmpty) {
      Customers.ddl.create
    }
  }




}
