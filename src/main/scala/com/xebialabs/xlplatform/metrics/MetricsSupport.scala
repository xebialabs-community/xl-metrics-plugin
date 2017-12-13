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

import com.xebialabs.deployit.jcr._
import com.xebialabs.deployit.plugin.api.reflect.Type
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem
import com.xebialabs.deployit.repository.{ConfigurationItemData, SearchParameters}
import grizzled.slf4j.Logging

import scala.collection.JavaConversions._

trait MetricsSupport extends ServicesSupport with Logging with MetricsConfig {

  val RESULTS_PER_PAGE = 1000

  case class CisCount(cisCounts: Map[String, Int] = Map(), revisionsCounts: Map[String, Int] = Map())

  def collectMetrics(): Metrics = {
    
    logger.info("Starting calculation of metrics for this server")

    logger.info("Calculating number of CIs per type")

    val counts = getCisCounts()

    val queriesCount = queries.map { case (queryName, query) =>
      queryName -> executeQuery(SubTypesResolver.transform(query))
    }

    val metrics = Metrics(counts.cisCounts.values.sum, counts.cisCounts, counts.revisionsCounts, queriesCount)

    logger.info("Finished calculating the metrics")

    metrics
  }

  private def getCisCounts(pageNumber: Int = 0): CisCount = {
    val parameters = new SearchParameters()
    parameters
      .setType(Type.valueOf(classOf[ConfigurationItem]))
      .setResultsPerPage(RESULTS_PER_PAGE)
      .setPage(pageNumber)
      .setDepth(0)

    logger.info(s"Fetching CIs ${pageNumber * RESULTS_PER_PAGE}-${(pageNumber + 1) * RESULTS_PER_PAGE}")

    val cis = repositoryService.list(parameters)
    val cisCountPerType = cis.groupBy(_.getType.toString).map { case (k, v) => k -> v.length }
    val revisionsCountPerType: Map[String, Int] =
      if (countVersions) {
        cis.foldLeft(Map.empty[String, Int]) { (acc, ci) =>
          merge(acc, Map(ci.getType.toString -> revisionsCount(ci)))
        }
      } else {
        Map.empty
      }

    if (cisCountPerType.isEmpty) {
      CisCount()
    } else {
      val counts = getCisCounts(pageNumber + 1)
      CisCount(merge(counts.cisCounts, cisCountPerType), merge(counts.revisionsCounts, revisionsCountPerType))
    }
  }

  private def executeQuery(query: String, pageNumber: Int = 0): Int = {
    val queryTemplate = new JcrQueryTemplate(query)
    queryTemplate.setDepth(0)
      .setResultsPerPage(RESULTS_PER_PAGE)
      .setPage(pageNumber)

    logger.info(s"Fetching results ${pageNumber * RESULTS_PER_PAGE}-${(pageNumber + 1) * RESULTS_PER_PAGE} for custom query: $query")

    val cis = repositoryService.list(queryTemplate)
    if (cis.isEmpty) 0 else cis.size() + executeQuery(query, pageNumber + 1)
  }

  private def revisionsCount(ci: ConfigurationItemData): Int =
    historyService.getVersionRevisions(ci.getId).size() - 1

  private def merge(map1: Map[String, Int], map2: Map[String, Int]): Map[String, Int] =
    map1 ++ map2.map{ case (k,v) => k -> (v + map1.getOrElse(k,0)) }
}
