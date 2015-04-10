/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.xebialabs.xlplatform.endpoints.AuthenticatedData
import spray.http.StatusCodes.BadRequest
import spray.routing.HttpService

import scala.concurrent._
import scala.concurrent.duration._

case object MetricsRequest
case class MetricsSuccess(metrics: Metrics)
case class MetricsFailure(error: Throwable)

trait MetricsRoute extends HttpService {

  val metricsActor = actorRefFactory.actorOf(MetricsActor.props)

  val route = (auth: AuthenticatedData) =>
    path("xl-metrics") {
      rejectNonAdmin(auth) {
        get {
          import com.xebialabs.xlplatform.metrics.MetricsProtocol._
          import spray.httpx.SprayJsonSupport._

          implicit val timeout = Timeout(10.seconds)
          implicit val dispatcher = actorRefFactory.dispatcher

          onSuccess(metricsActor.ask(MetricsRequest).mapTo[Result]) {
            case metricsInError: MetricsInError => complete(BadRequest, metricsInError)
            case result => complete(result)
          }
        }
      }
    }
}

class MetricsActor extends Actor with MetricsSupport {
  def receive = idle

  def idle: Actor.Receive = {
    case MetricsRequest =>
      startWork()
      sender() ! MetricsStarted("Query started, please retry in a few moments.")
      context become inProgress
  }

  def inProgress: Actor.Receive = {
    case MetricsRequest =>
      sender() ! MetricsInProgress("Query in progress, please retry in a few moments.")
    case MetricsSuccess(metrics) =>
      context become completed(metrics)
    case MetricsFailure(error) =>
      logger.error("Error while building metrics: ", error)
      context become failed(error)
  }

  def completed(metrics: Metrics): Actor.Receive = {
    case MetricsRequest =>
      sender() ! metrics
      context become idle
  }

  def failed(throwable: Throwable): Actor.Receive = {
    case MetricsRequest =>
      sender() ! MetricsInError(throwable.getMessage)
      context become idle
  }

  private def startWork() {
    import context.dispatcher

    val f = future {
      collectMetrics()
    }
    f onSuccess {
      case metrics => self ! MetricsSuccess(metrics)
    }
    f onFailure {
      case e => self ! MetricsFailure(e)
    }
  }
}

object MetricsActor {
  def props = Props(classOf[MetricsActor])
}
