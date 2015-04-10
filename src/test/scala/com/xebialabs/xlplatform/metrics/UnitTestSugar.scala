/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import com.xebialabs.deployit.booter.local.LocalBooter
import org.scalatest._

trait UnitTestSugar extends FunSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  override protected def beforeAll() {
    LocalBooter.bootWithoutGlobalContext()
  }
}
