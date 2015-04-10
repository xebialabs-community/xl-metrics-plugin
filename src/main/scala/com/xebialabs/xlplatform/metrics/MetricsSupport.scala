/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
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

  def collectMetrics() = {
    
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
    val revisionsCountPerType = cis.foldLeft(Map.empty[String, Int]) { (acc, ci) =>
      merge(acc, Map(ci.getType.toString -> revisionsCount(ci)))
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
    jcrHistoryService.getVersionRevisions(ci.getId).size() - 1

  private def merge(map1: Map[String, Int], map2: Map[String, Int]): Map[String, Int] =
    map1 ++ map2.map{ case (k,v) => k -> (v + map1.getOrElse(k,0)) }
}
