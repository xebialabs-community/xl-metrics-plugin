/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat, _}

sealed trait Result

case class Metrics(totalCisCount: Int,
                   cisCounts: Map[String, Int],
                   revisionsCounts: Map[String, Int],
                   customQueriesCounts: Map[String, Int],
                   status: String = "completed") extends Result

case class MetricsStarted(message: String, status: String = "started") extends Result

case class MetricsInProgress(message: String, status: String = "in_progress") extends Result

case class MetricsInError(message: String, status: String = "failed") extends Result

trait MetricsProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val metricsFormat = jsonFormat5(Metrics)
  implicit val metricsStartedFormat = jsonFormat2(MetricsStarted)
  implicit val metricsInProgressFormat = jsonFormat2(MetricsInProgress)
  implicit val metricsInErrorFormat = jsonFormat2(MetricsInError)

  implicit object ResultProtocol extends RootJsonFormat[Result] {
    //noinspection NotImplementedCode
    def read(json: JsValue): Result = ???

    def write(result: Result): JsValue = {
      result match {
        case x: Metrics => x.toJson
        case x: MetricsStarted => x.toJson
        case x: MetricsInProgress => x.toJson
        case x: MetricsInError => x.toJson
      }
    }
  }

}
