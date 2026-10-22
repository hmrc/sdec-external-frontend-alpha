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
import models.sdec.AccessLevel
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class ThreadViewedAuditEventSpec extends AnyFreeSpec with Matchers with ExternalUserFixtures {

  private val threadReference = "THREAD1000AA"

  private val threadViewedAuditEvent = ThreadViewedAuditEvent(
    customerIdentifier = customerIdentifier,
    affinityGroup = Some("Individual"),
    firstName = Some("John"),
    lastName = Some("Doe"),
    email = Some("johndoe@example.com"),
    nino = Some("AA000000A"),
    threadReference = threadReference,
    accessLevel = AccessLevel.Full
  )

  "ThreadViewedAuditEvent" - {
    "auditType" - {
      "must be ThreadViewed" in {
        threadViewedAuditEvent.auditType mustBe "ThreadViewed"
      }
    }

    "detail" - {
      "must contain only the agreed fields" in {
        threadViewedAuditEvent.detail mustBe Json.obj(
          "customerIdentifier" -> Json.obj(
            "provider"   -> "ONE_LOGIN",
            "providerId" -> customerIdentifier.providerId
          ),
          "affinityGroup"   -> "Individual",
          "firstName"       -> "John",
          "lastName"        -> "Doe",
          "email"           -> "johndoe@example.com",
          "nino"            -> "AA000000A",
          "threadReference" -> threadReference,
          "accessLevel"     -> "Full"
        )
      }

      "must omit affinityGroup when it is not present" in {
        threadViewedAuditEvent.copy(affinityGroup = None).detail.keys must not contain "affinityGroup"
      }
    }

    "extendedDataEvent" - {
      "must carry the given audit source, the audit type and the detail" in {
        val dataEvent = threadViewedAuditEvent.extendedDataEvent("test-audit-source")

        dataEvent.auditSource mustBe "test-audit-source"
        dataEvent.auditType mustBe threadViewedAuditEvent.auditType
        dataEvent.detail mustBe threadViewedAuditEvent.detail
      }
    }
  }

}
