package de.guderlei.spray.api

import akka.actor.{ActorRefFactory, Props, Actor}
import spray.routing._
import de.guderlei.spray.core._
import de.guderlei.spray.domain._
import reflect.ClassTag
import spray.httpx.Json4sSupport
import org.json4s.DefaultFormats
import spray.util.LoggingContext

// magic import

import scala.concurrent.Future

// magic import


/**
 * Actor to provide the routes of the rest services
 */
trait TodoWebServiceActor extends HttpService with PerRequestCreator with Json4sSupport {

  implicit def actorRefFactory : ActorRefFactory

  val json4sFormats = DefaultFormats

  val itemRoute =
    path("items" / LongNumber) {
          val logger = LoggingContext.fromActorRefFactory
      id: Long =>
        get {
          logger.debug("GET ROOT PATH")
          rejectEmptyResponse {
            handlePerRequest {
              Get(id)
            }
          }
        } ~ put {
          entity(as[TodoItem]) {
            item =>
              handlePerRequest {
                Update(new TodoItem(id, item.dueDate, item.text))
              }
          }
        } ~ delete {
          handlePerRequest {
            Delete(id)
          }
        }
    } ~ path("items") {
      get {

        handlePerRequest {
          All
        }
      }
    } ~ post {
      entity(as[TodoItem]) {
        item =>
          handlePerRequest {
            Create(item.dueDate, item.text)
          }
      }
    }


  def handlePerRequest(message: RequestMessage): Route =
    ctx => perRequest(actorRefFactory, ctx, Props[TodoItemActor], message)
}

class TodoWebService extends Actor with TodoWebServiceActor {

  implicit def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(itemRoute)


}



