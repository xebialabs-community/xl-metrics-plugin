/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlplatform

import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive0, Directives}
import com.xebialabs.deployit.security.permission.PermissionHelper
import com.xebialabs.xlplatform.endpoints.AuthenticatedData

package object metrics extends Directives {
  def rejectNonAdmin(auth: AuthenticatedData): Directive0 = {
    PermissionHelper.isAdmin(auth.toAuthentication) match {
      case false => reject(AuthorizationFailedRejection)
      case true => pass
    }
  }
}
