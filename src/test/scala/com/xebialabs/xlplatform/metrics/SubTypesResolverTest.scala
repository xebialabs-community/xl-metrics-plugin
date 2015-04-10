/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SubTypesResolverTest extends UnitTestSugar {
  describe("SubTypesResolverTest") {
    it("should resolve subtypes when used once") {
      val transformedText = SubTypesResolver.transform("isSubTypeOf[ci:unit.BaseType]")
      transformedText should be("(ci.[$configuration.item.type] = 'unit.SubType2' OR ci.[$configuration.item.type] = 'unit.SubType1')")
    }
    it("should include prefix and suffix") {
      val transformedText = SubTypesResolver.transform("prefix isSubTypeOf[ci:unit.BaseType] suffix")
      transformedText should startWith("prefix ")
      transformedText should endWith(" suffix")
    }
    it("should resolve subtypes when used several times") {
      val transformedText = SubTypesResolver.transform("isSubTypeOf[ci:unit.BaseType] something isSubTypeOf[test:unit.AnotherBaseType]")
      transformedText should be(
        "(ci.[$configuration.item.type] = 'unit.SubType2' OR ci.[$configuration.item.type] = 'unit.SubType1') something " +
          "(test.[$configuration.item.type] = 'unit.AnotherSubType')")
    }
  }
}
