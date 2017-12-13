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
