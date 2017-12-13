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
