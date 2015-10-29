package com.mentatlabs.example

import java.sql.Connection
import org.postgresql.PGConnection
import org.postgresql.ds.PGSimpleDataSource
import scala.collection.mutable.ArrayBuffer

class PostgresListener(ds: PGSimpleDataSource) {
  def getConnection() = ds.getConnection() match {
    case pg: PGConnection => pg
    case _ => sys.error("Could not retrieve PG connection!")
  }


  def readNotification(channels: String) = {
    val directive = channels.split('/').map(c => s"""LISTEN "$c";""").mkString("\n")
    println(directive)

    val conn = getConnection()
    val stmt = conn.createStatement()
    stmt.execute(directive)
    stmt.close()

    val result = awaitNotification(conn)
    conn.close()
    result
  }

  private def awaitNotification(conn: Connection with PGConnection): String = {
    val stmt = conn.createStatement()
    stmt.execute(";")
    stmt.close()

    val notifications = conn.getNotifications()
    if (notifications != null) {
      notifications map { n =>
        n.getName + ": " + n.getParameter
      } mkString "\n"
    } else {
      Thread.sleep(100) // JDBC does not support async notifications :(
      awaitNotification(conn)
    }
  }

  def waitForTableUpdate(name: String) = {
    println(readNotification(name) + "(table updated)")

    val conn = getConnection()
    try {
      val st = conn.prepareStatement(s"TABLE $name ORDER BY 1;")
      try {
        val rs = st.executeQuery()
        try {

          val md = rs.getMetaData
          val columns = (1 to md.getColumnCount) map {
            md.getColumnName
          }

          val rows = new ArrayBuffer[Seq[String]]
          while (rs.next()) {
            rows += (1 to columns.length) map {
              rs.getString
            }
          }

          (columns, rows)

        } finally rs.close()
      } finally st.close()
    } finally conn.close()
  }
}
