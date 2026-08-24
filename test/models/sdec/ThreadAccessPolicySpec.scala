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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ThreadAccessPolicySpec extends AnyFreeSpec with Matchers {

  private val olId = CustomerIdentifier(IdentityProvider.OneLogin, "sub-123")
  private val ggId =
    CustomerIdentifier(IdentityProvider.GovernmentGateway, "cred-123")

  private def attributes(
    email:        Option[String] = None,
    nino:         Option[String] = None,
    enrolmentKey: Option[String] = None
  ): UnlinkedExternalUser =
    UnlinkedExternalUser(email = email, nino = nino, enrolmentKey = enrolmentKey)

  private val noAttrs = UnlinkedExternalUser.empty
  private val noClaim = UnlinkedExternalUser.empty

  "ThreadAccessPolicy.authoriseAccess" - {

    "for a thread not yet linked (first login)" - {
      "links when the NINO matches the claim" in {
        val signedIn = attributes(nino = Some("AA000000A"))
        val claim    = attributes(nino = Some("aa000000a"))
        ThreadAccessPolicy.authoriseAccess(olId, signedIn, claim, None) mustBe
          AccessDecision.Link(olId)
      }

      "links when the email matches" in {
        val signedIn = attributes(email = Some("JohnDoe@example.com"))
        val claim    = attributes(email = Some("johndoe@example.com"))
        ThreadAccessPolicy.authoriseAccess(olId, signedIn, claim, None) mustBe
          AccessDecision.Link(olId)
      }

      "links when the enrolment key matches (GG)" in {
        val signedIn = attributes(enrolmentKey = Some("HMRC-MTD-VAT~VRN~123"))
        val claim    = attributes(enrolmentKey = Some("hmrc-mtd-vat~vrn~123"))
        ThreadAccessPolicy.authoriseAccess(ggId, signedIn, claim, None) mustBe
          AccessDecision.Link(ggId)
      }

      "denies when nothing matches" in {
        val signedIn = attributes(email = Some("wrong@example.com"))
        val claim    = attributes(email = Some("right@example.com"))
        ThreadAccessPolicy.authoriseAccess(olId, signedIn, claim, None) mustBe
          AccessDecision.Denied
      }

      "denies when the claim is empty" in {
        ThreadAccessPolicy.authoriseAccess(olId, noAttrs, noClaim, None) mustBe
          AccessDecision.Denied
      }
    }

    "for a thread already linked to a customer (subsequent logins)" - {
      "grants the same identity" in {
        ThreadAccessPolicy.authoriseAccess(olId, noAttrs, noClaim, Some(olId)) mustBe
          AccessDecision.Granted
      }

      "denies a different identity" in {
        ThreadAccessPolicy.authoriseAccess(olId, noAttrs, noClaim, Some(ggId)) mustBe
          AccessDecision.Denied
      }

      "denies the same providerId under a different provider" in {
        val ggLookalike =
          CustomerIdentifier(IdentityProvider.GovernmentGateway, "sub-123")
        ThreadAccessPolicy.authoriseAccess(
          ggLookalike,
          noAttrs,
          noClaim,
          Some(olId)
        ) mustBe AccessDecision.Denied
      }
    }

  }
}
