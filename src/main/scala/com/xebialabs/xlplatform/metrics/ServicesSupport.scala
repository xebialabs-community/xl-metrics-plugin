/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import com.xebialabs.deployit.repository.{HistoryService, RepositoryService}

trait ServicesSupport {
  def repositoryService: RepositoryService = ServicesHolder.repositoryService
  def historyService: HistoryService = ServicesHolder.historyService
}
