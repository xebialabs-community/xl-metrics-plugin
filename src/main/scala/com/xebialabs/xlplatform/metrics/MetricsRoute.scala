/**
 * Copyright ${year} ${name}
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xebialabs.xlplatform.metrics

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.xebialabs.xlplatform.endpoints.{AuthenticatedData, ExtensionRoutes}

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case object MetricsRequest

case class MetricsSuccess(metrics: Metrics)

case class MetricsFailure(error: Throwable)

class MetricsRoute extends ExtensionRoutes with MetricsProtocol {

  override def route(system: ActorSystem): (AuthenticatedData) => Route = {
    val metricsActor = system.actorOf(MetricsActor.props)
    (auth: AuthenticatedData) =>
      path("xl-metrics") {
        rejectNonAdmin(auth) {
          get {

            implicit val timeout = Timeout(10.seconds)
            implicit val dispatcher = system.dispatcher

            onSuccess(metricsActor.ask(MetricsRequest).mapTo[Result]) {
              case metricsInError: MetricsInError => complete(BadRequest, metricsInError)
              case result => complete(result)
            }
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

    Future {
      collectMetrics()
    }.onComplete {
      case Success(metrics) => self ! MetricsSuccess(metrics)
      case Failure(e) => self ! MetricsFailure(e)
    }
  }
}

object MetricsActor {
  def props = Props(classOf[MetricsActor])
}
