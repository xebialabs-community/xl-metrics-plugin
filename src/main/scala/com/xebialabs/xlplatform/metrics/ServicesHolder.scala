/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import javax.annotation.PostConstruct

import com.xebialabs.deployit.repository.{HistoryService, RepositoryService}
import com.xebialabs.xlplatform.repository.XlRepositoryConfig
import grizzled.slf4j.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ServicesHolder @Autowired()(repositoryService: RepositoryService,
                                  historyService: HistoryService,
                                  xlRepositoryConfig: XlRepositoryConfig) extends Logging {
  @PostConstruct def initServices(): Unit = {
    ServicesHolder.repositoryService = repositoryService
    ServicesHolder.historyService = historyService
    ServicesHolder.xlRepositoryConfig = xlRepositoryConfig
    logger.info(s"Initialized xl-metrics for product ${Option(xlRepositoryConfig).map(_.repositoryName).orNull}")
  }
}

object ServicesHolder {
  var repositoryService: RepositoryService = _
  var historyService: HistoryService = _
  var xlRepositoryConfig: XlRepositoryConfig = _
}
