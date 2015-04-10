/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform.metrics

import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry._
import com.xebialabs.deployit.plugin.api.reflect.Type

import scala.collection.JavaConversions._

case class Expression(matchedExpression: String, replacement: String)

object SubTypesResolver {
  val pattern = "isSubTypeOf\\[(\\w+):([\\w\\.]+)\\]".r

  def transform(text: String): String = {
    val replacedExpressions = pattern.findAllIn(text).matchData.map(
      matcher => {
        val wholeExpression = matcher.group(0)
        val identifier = matcher.group(1)
        val ciType = Type.valueOf(matcher.group(2))

        val whereConditions = getSubtypes(ciType).map(subType =>
          identifier + ".[$configuration.item.type] = '" + subType + "'"
        )
        Expression(wholeExpression, "(" + whereConditions.mkString(" OR ") + ")")
      }
    )

    replacedExpressions.foldLeft(text)((text, current) =>
      text.replace(current.matchedExpression, current.replacement)
    )
  }
}
