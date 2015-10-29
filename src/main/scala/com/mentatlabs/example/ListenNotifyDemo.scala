package com.mentatlabs.example

import akka.io.IO
import akka.actor._
import spray.can.Http
import spray.routing.HttpService
import org.postgresql.ds.PGSimpleDataSource

object ListenNotifyDemo extends App {
  implicit val system = ActorSystem("listen-notify")

  val webActor = system.actorOf(Props[WebActor])
  IO(Http) ! Http.Bind(webActor, interface = "127.0.0.1", port = 8080)

  println("Press Enter to stop ...")
  scala.io.StdIn.readLine()

  system.shutdown()
}

class WebActor extends Actor with HttpService {
  val ds = new PGSimpleDataSource()
  ds.setUrl("jdbc:postgresql://127.0.0.1:5432/ln?user=ln&password=ln")

  private val listener = new PostgresListener(ds)

  def route = (
    path("listen" / RestPath) ( channels =>
      complete {
        listener.readNotification(channels.toString)
      }
    )
    ~
    path("table" / Segment) { table =>
      complete {
        val (columns, rows) = listener.waitForTableUpdate(table)

        <html>
          <meta http-equiv="refresh" content="0"/>
          <table>
            <thead>
              <tr>
                { columns map { col => <th>{ col }</th> } }
              </tr>
            </thead>
            <tbody>
              { rows map { row =>
                <tr>
                  { row map { cell => <td>{ cell }</td> } }
                </tr>
              }}
            </tbody>
          </table>
        </html>
      }
    }
  )

  def actorRefFactory = context
  def receive = runRoute(detach()(route))
}
