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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class FileUploadAuditEventsSpec extends AnyFreeSpec with Matchers with ExternalUserFixtures {

  private val pdfFile = AuditedFile(contentType = Some("application/pdf"), sizeBytes = 120394L)
  private val pngFile = AuditedFile(contentType = Some("image/png"), sizeBytes = 50211L)

  private val customerIdentifierJson = Json.obj(
    "provider"   -> "ONE_LOGIN",
    "providerId" -> customerIdentifier.providerId
  )

  "SingleFileUploadedAuditEvent" - {
    val event = SingleFileUploadedAuditEvent(
      customerIdentifier = customerIdentifier,
      affinityGroup = Some("Individual"),
      firstName = Some("John"),
      lastName = Some("Doe"),
      email = Some("johndoe@example.com"),
      nino = Some("AA000000A"),
      file = pdfFile
    )

    "auditType must be SingleFileUploaded" in {
      event.auditType mustBe "SingleFileUploaded"
    }

    "detail must contain only the agreed fields" in {
      event.detail mustBe Json.obj(
        "customerIdentifier" -> customerIdentifierJson,
        "affinityGroup"      -> "Individual",
        "firstName"          -> "John",
        "lastName"           -> "Doe",
        "email"              -> "johndoe@example.com",
        "nino"               -> "AA000000A",
        "file"               -> Json.obj(
          "contentType" -> "application/pdf",
          "sizeBytes"   -> 120394
        )
      )
    }

    "detail must omit contentType when it is not known" in {
      val detail = event.copy(file = pdfFile.copy(contentType = None)).detail

      (detail \ "file").as[play.api.libs.json.JsObject].keys must not contain "contentType"
    }
  }

  "MultipleFilesUploadedAuditEvent" - {
    val multipleFilesUploadedAuditEvent = MultipleFilesUploadedAuditEvent(
      customerIdentifier = customerIdentifier,
      affinityGroup = Some("Individual"),
      firstName = Some("John"),
      lastName = Some("Doe"),
      email = Some("johndoe@example.com"),
      nino = Some("AA000000A"),
      files = Seq(pdfFile, pngFile)
    )

    "auditType must be MultipleFilesUploaded" in {
      multipleFilesUploadedAuditEvent.auditType mustBe "MultipleFilesUploaded"
    }

    "detail must contain the files, the file count and the total size" in {
      multipleFilesUploadedAuditEvent.detail mustBe Json.obj(
        "customerIdentifier" -> customerIdentifierJson,
        "affinityGroup"      -> "Individual",
        "firstName"          -> "John",
        "lastName"           -> "Doe",
        "email"              -> "johndoe@example.com",
        "nino"               -> "AA000000A",
        "files"              -> Json.arr(
          Json.obj("contentType" -> "application/pdf", "sizeBytes" -> 120394),
          Json.obj("contentType" -> "image/png", "sizeBytes"       -> 50211)
        ),
        "fileCount"      -> 2,
        "totalSizeBytes" -> 170605
      )
    }
  }

}
