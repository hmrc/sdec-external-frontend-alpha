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

package models.audit

import fixtures.ExternalUserFixtures
import models.sdec.AccessValidationFailure
import models.sdec.AccessValidationFailure.EmailMismatch
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class ThreadAccessAttemptAuditEventSpec extends AnyFreeSpec with Matchers with ExternalUserFixtures {

  private val threadReference = "THREAD1000AA"

  private val grantedEvent = ThreadAccessAttemptAuditEvent(
    customerIdentifier = customerIdentifier,
    affinityGroup = Some("Individual"),
    confidenceLevel = 200,
    threadReference = threadReference,
    outcome = ThreadAccessOutcome.Granted,
    failureReasons = List.empty[AccessValidationFailure]
  )

  "ThreadAccessAttemptAuditEvent" - {

    "auditType" - {
      "must be ThreadAccessAttempt" in {
        grantedEvent.auditType mustBe "ThreadAccessAttempt"
      }
    }

    "detail" - {
      "must contain only the agreed fields for a granted attempt" in {
        grantedEvent.detail mustBe Json.obj(
          "customerIdentifier" -> Json.obj(
            "provider"   -> "ONE_LOGIN",
            "providerId" -> customerIdentifier.providerId
          ),
          "affinityGroup"   -> "Individual",
          "confidenceLevel" -> 200,
          "threadReference" -> threadReference,
          "outcome"         -> "Granted",
          "failureReasons"  -> Json.arr()
        )
      }

      "must include the failure reason for a denied attempt" in {
        val deniedEvent = grantedEvent.copy(
          outcome = ThreadAccessOutcome.Denied,
          failureReasons = List(EmailMismatch)
        )

        deniedEvent.detail mustBe Json.obj(
          "customerIdentifier" -> Json.obj(
            "provider"   -> "ONE_LOGIN",
            "providerId" -> customerIdentifier.providerId
          ),
          "affinityGroup"   -> "Individual",
          "confidenceLevel" -> 200,
          "threadReference" -> threadReference,
          "outcome"         -> "Denied",
          "failureReasons"  -> Json.arr("EmailMismatch")
        )
      }

      "must write each outcome with its agreed name" in {
        val expectedNames = Map(
          ThreadAccessOutcome.Granted  -> "Granted",
          ThreadAccessOutcome.Limited  -> "Limited",
          ThreadAccessOutcome.Denied   -> "Denied",
          ThreadAccessOutcome.NotFound -> "NotFound",
          ThreadAccessOutcome.Error    -> "Error"
        )

        expectedNames.keySet mustBe ThreadAccessOutcome.values.toSet

        expectedNames.foreach { (outcome, name) =>
          (grantedEvent.copy(outcome = outcome).detail \ "outcome").as[String] mustBe name
        }
      }

      "must omit affinityGroup when it is not present" in {
        grantedEvent.copy(affinityGroup = None).detail.keys must not contain "affinityGroup"
      }
    }
  }

}
