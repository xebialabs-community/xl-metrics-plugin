/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import javax.annotation.PostConstruct

import com.xebialabs.deployit.repository.{JcrHistoryService, RepositoryService}
import com.xebialabs.xlplatform.repository.XlRepositoryConfig
import grizzled.slf4j.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ServicesHolder @Autowired()(repositoryService: RepositoryService,
                                  jcrHistoryService: JcrHistoryService,
                                  xlRepositoryConfig: XlRepositoryConfig) extends Logging {
  @PostConstruct def initServices() = {
    ServicesHolder.repositoryService = repositoryService
    ServicesHolder.jcrHistoryService = jcrHistoryService
    ServicesHolder.xlRepositoryConfig = xlRepositoryConfig
    logger.info(s"Initialized xl-metrics for product ${Option(xlRepositoryConfig).map(_.repositoryName).orNull}")
  }
}

object ServicesHolder {
  var repositoryService: RepositoryService = null
  var jcrHistoryService: JcrHistoryService = null
  var xlRepositoryConfig: XlRepositoryConfig = null
}
