package com.example.mysql

import com.example.configuration.CustomerSystemConfiguration
import scala.slick.session.Database
import com.example.domain.{FailureType, Customer, Customers}
import scala.Some
import scala.slick.driver.MySQLDriver.simple.Database.threadLocalSession
import scala.slick.driver.MySQLDriver.simple._
import slick.jdbc.meta.MTable
import java.sql.SQLException
import com.example.domain.Failure



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


  /**
   * Retrieves specific customer from database.
   *
   * @param id id of the customer to retrieve
   * @return customer entity with specified id
   */
  def get(id: Long): Either[Failure, Customer] = {
    try {
      db.withSession {
        Customers.findById(id).firstOption match {
          case Some(customer: Customer) =>
            Right(customer)
          case _ =>
            Left(notFoundError(id))
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
   * Produce database error description.
   *
   * @param e SQL Exception
   * @return database error description
   */
  protected def databaseError(e: SQLException) =
    Failure("%d: %s".format(e.getErrorCode, e.getMessage), FailureType.DatabaseFailure)

  /**
   * Produce customer not found error description.
   *
   * @param customerId id of the customer
   * @return not found error description
   */
  protected def notFoundError(customerId: Long) =
    Failure("Customer with id=%d does not exist".format(customerId), FailureType.NotFound)




}
