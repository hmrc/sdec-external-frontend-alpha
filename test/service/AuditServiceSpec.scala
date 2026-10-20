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

package service

import fixtures.ExternalUserFixtures
import models.UploadedFile
import models.audit.ThreadAccessOutcome
import models.sdec.AccessValidationFailure.EmailMismatch
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with BeforeAndAfterEach
    with ExternalUserFixtures {

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = ExecutionContext.global

  private val auditSource     = "test-audit-source"
  private val threadReference = "THREAD1000AA"
  private val auditFailure    = AuditResult.Failure("Event sending failed")

  private val pdfFile = UploadedFile(
    filename = "John Smith AA000000A.pdf",
    contentType = Some("application/pdf"),
    size = 120394L,
    location = Paths.get("uploads/John Smith AA000000A.pdf")
  )

  private val pngFile = UploadedFile(
    filename = "passport-scan.png",
    contentType = Some("image/png"),
    size = 50211L,
    location = Paths.get("uploads/passport-scan.png")
  )

  private val customerIdentifierJson = Json.obj(
    "provider"   -> "ONE_LOGIN",
    "providerId" -> customerIdentifier.providerId
  )

  private val mockAuditConnector: AuditConnector = mock[AuditConnector]
  private val auditService:       AuditService   = AuditService(mockAuditConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockAuditConnector)
    when(mockAuditConnector.auditingConfig)
      .thenReturn(AuditingConfig(consumer = None, enabled = true, auditSource = auditSource, auditSentHeaders = false))
    when(mockAuditConnector.sendExtendedEvent(any())(using any(), any()))
      .thenReturn(Future.successful(AuditResult.Success))
  }

  private def capturedEvent(): ExtendedDataEvent = {
    val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
    verify(mockAuditConnector).sendExtendedEvent(captor.capture())(using any(), any())
    captor.getValue
  }

  private def connectorReturns(result: AuditResult): Unit =
    when(mockAuditConnector.sendExtendedEvent(any())(using any(), any()))
      .thenReturn(Future.successful(result))

  "AuditService" - {
    "auditThreadViewed" - {
      "must send a ThreadViewed event with the audit source from config" in {
        auditService.auditThreadViewed(externalUser, threadReference).futureValue mustBe AuditResult.Success

        val event = capturedEvent()
        event.auditSource mustBe auditSource
        event.auditType mustBe "ThreadViewed"
        event.detail mustBe Json.obj(
          "customerIdentifier" -> customerIdentifierJson,
          "affinityGroup"      -> "Individual",
          "threadReference"    -> threadReference
        )
      }

      "must return the result from the connector unchanged" in {
        connectorReturns(auditFailure)

        auditService.auditThreadViewed(externalUser, threadReference).futureValue mustBe auditFailure
      }
    }

    "auditThreadAccessAttempt" - {
      "must send a ThreadAccessAttempt event with the outcome and failure reason" in {
        auditService
          .auditThreadAccessAttempt(externalUser, threadReference, ThreadAccessOutcome.Denied, List(EmailMismatch))
          .futureValue mustBe AuditResult.Success

        val event = capturedEvent()
        event.auditSource mustBe auditSource
        event.auditType mustBe "ThreadAccessAttempt"
        event.detail mustBe Json.obj(
          "customerIdentifier" -> customerIdentifierJson,
          "affinityGroup"      -> "Individual",
          "confidenceLevel"    -> 200,
          "threadReference"    -> threadReference,
          "outcome"            -> "Denied",
          "failureReasons"     -> Json.arr("EmailMismatch")
        )
      }

      "must return the result from the connector unchanged" in {
        connectorReturns(auditFailure)

        auditService
          .auditThreadAccessAttempt(externalUser, threadReference, ThreadAccessOutcome.Granted, Nil)
          .futureValue mustBe auditFailure
      }
    }

    "auditSingleFileUploaded" - {
      "must send a SingleFileUploaded event with the content type and size" in {
        auditService.auditSingleFileUploaded(externalUser, pdfFile).futureValue mustBe AuditResult.Success

        val event = capturedEvent()
        event.auditSource mustBe auditSource
        event.auditType mustBe "SingleFileUploaded"
        event.detail mustBe Json.obj(
          "customerIdentifier" -> customerIdentifierJson,
          "affinityGroup"      -> "Individual",
          "file"               -> Json.obj(
            "contentType" -> "application/pdf",
            "sizeBytes"   -> 120394
          )
        )
      }

      "must not send the file name or location" in {
        auditService.auditSingleFileUploaded(externalUser, pdfFile).futureValue

        val sentDetail            = Json.stringify(capturedEvent().detail)
        val fileNamesAndLocations = Seq(pdfFile.filename, pdfFile.location.toString)

        fileNamesAndLocations.filter(sentDetail.contains) mustBe empty
      }

      "must return the result from the connector unchanged" in {
        connectorReturns(auditFailure)

        auditService.auditSingleFileUploaded(externalUser, pdfFile).futureValue mustBe auditFailure
      }
    }

    "auditMultipleFilesUploaded" - {
      "must send a MultipleFilesUploaded event with the files, count and total size" in {
        auditService
          .auditMultipleFilesUploaded(externalUser, Seq(pdfFile, pngFile))
          .futureValue mustBe AuditResult.Success

        val event = capturedEvent()
        event.auditSource mustBe auditSource
        event.auditType mustBe "MultipleFilesUploaded"
        event.detail mustBe Json.obj(
          "customerIdentifier" -> customerIdentifierJson,
          "affinityGroup"      -> "Individual",
          "files"              -> Json.arr(
            Json.obj("contentType" -> "application/pdf", "sizeBytes" -> 120394),
            Json.obj("contentType" -> "image/png", "sizeBytes"       -> 50211)
          ),
          "fileCount"      -> 2,
          "totalSizeBytes" -> 170605
        )
      }

      "must not send any file name or location" in {
        auditService.auditMultipleFilesUploaded(externalUser, Seq(pdfFile, pngFile)).futureValue

        val sentDetail            = Json.stringify(capturedEvent().detail)
        val fileNamesAndLocations = Seq(pdfFile, pngFile).flatMap(file => Seq(file.filename, file.location.toString))

        fileNamesAndLocations.filter(sentDetail.contains) mustBe empty
      }

      "must return the result from the connector unchanged" in {
        connectorReturns(auditFailure)

        auditService.auditMultipleFilesUploaded(externalUser, Seq(pdfFile, pngFile)).futureValue mustBe auditFailure
      }
    }
  }

}
