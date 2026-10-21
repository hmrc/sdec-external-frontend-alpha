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

import models.UploadedFile
import models.audit.*
import models.sdec.{AccessLevel, AccessValidationFailure, ExternalUser}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (auditConnector: AuditConnector)(using ec: ExecutionContext)
    extends AuditServiceAlgebra
    with Logging {

  private def auditSource: String = auditConnector.auditingConfig.auditSource

  def auditThreadViewed(
    user:            ExternalUser,
    threadReference: String,
    accessLevel:     AccessLevel
  )(using hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      ThreadViewedAuditEvent(
        customerIdentifier = user.id,
        affinityGroup = user.affinityGroup.map(_.toString),
        threadReference = threadReference,
        accessLevel = accessLevel
      ).extendedDataEvent(auditSource)
    )

  def auditThreadAccessAttempt(
    user:            ExternalUser,
    threadReference: String,
    outcome:         ThreadAccessOutcome,
    failureReasons:  List[AccessValidationFailure]
  )(using hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      ThreadAccessAttemptAuditEvent(
        customerIdentifier = user.id,
        affinityGroup = user.affinityGroup.map(_.toString),
        confidenceLevel = user.confidenceLevel.level,
        threadReference = threadReference,
        outcome = outcome,
        failureReasons = failureReasons
      ).extendedDataEvent(auditSource)
    )

  def auditSingleFileUploaded(
    user: ExternalUser,
    file: UploadedFile
  )(using hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      SingleFileUploadedAuditEvent(
        customerIdentifier = user.id,
        affinityGroup = user.affinityGroup.map(_.toString),
        file = AuditedFile(contentType = file.contentType, sizeBytes = file.size)
      ).extendedDataEvent(auditSource)
    )

  def auditMultipleFilesUploaded(
    user:  ExternalUser,
    files: Seq[UploadedFile]
  )(using hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      MultipleFilesUploadedAuditEvent(
        customerIdentifier = user.id,
        affinityGroup = user.affinityGroup.map(_.toString),
        files = files.map(file => AuditedFile(contentType = file.contentType, sizeBytes = file.size))
      ).extendedDataEvent(auditSource)
    )

}
