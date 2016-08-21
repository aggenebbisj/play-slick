package controllers

import akka.actor.ActorSystem
import javax.inject._

import play.api._
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._

/**
 * This controller creates an `Action` that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
 * run code after a delay.
 * @param exec We need an `ExecutionContext` to execute our
 * asynchronous code.
 */
@Singleton
class AsyncController @Inject() (dbConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends Controller {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  private val Msgs = TableQuery[MsgTable]

  def message = Action.async {
    getFutureMessage.map(msg => msg.map(msg => Ok(msg.msg)).getOrElse(NotFound))
  }

  private def _findById(id: Long): DBIO[Option[Msg]] = Msgs.filter(_.id === id).result.headOption

  private def getFutureMessage: Future[Option[Msg]] = db.run(_findById(1))

  private class MsgTable(tag: Tag) extends Table[Msg](tag, "msg") {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def msg = column[String]("msg")

    def * = (id, msg) <> (Msg.tupled, Msg.unapply)
    def ? = (id.?, msg.?).shaped.<>({ r => import r._; _1.map(_ => Msg.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }

  case class Msg(id: Long, msg: String)

}
