/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.sdec

object ThreadAccessPolicy {

  def authoriseAccess(
    signedIn:           CustomerIdentifier,
    signedInAttributes: UnlinkedExternalUser,
    threadClaim:        UnlinkedExternalUser,
    linkedCustomer:     Option[CustomerIdentifier]
  ): AccessDecision =
    linkedCustomer match {
      case Some(linked) =>
        if linked == signedIn then AccessDecision.Granted
        else AccessDecision.Denied

      case None =>
        if matchesClaim(signedInAttributes, threadClaim) then AccessDecision.Link(signedIn)
        else AccessDecision.Denied
    }

  private def matchesClaim(
    signedIn: UnlinkedExternalUser,
    claim:    UnlinkedExternalUser
  ): Boolean = {
    def bothEqual(a: Option[String], b: Option[String]): Boolean =
      (a, b) match {
        case (Some(x), Some(y)) => x.equalsIgnoreCase(y)
        case _                  => false
      }

    bothEqual(signedIn.nino, claim.nino) ||
    bothEqual(signedIn.enrolmentKey, claim.enrolmentKey) ||
    bothEqual(signedIn.email, claim.email)
  }
}
