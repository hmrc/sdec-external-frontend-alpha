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
import models.audit.ThreadAccessOutcome
import models.sdec.{AccessLevel, AccessValidationFailure, ExternalUser}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.Future

trait AuditServiceAlgebra {

  // def audit(event: AuditEvent)(using hc: HeaderCarrier): Future[AuditResult]

  def auditThreadViewed(
    user:            ExternalUser,
    threadReference: String,
    accessLevel:     AccessLevel
  )(using hc: HeaderCarrier): Future[AuditResult]

  def auditThreadAccessAttempt(
    user:            ExternalUser,
    threadReference: String,
    outcome:         ThreadAccessOutcome,
    failureReasons:  List[AccessValidationFailure]
  )(using hc: HeaderCarrier): Future[AuditResult]

  def auditSingleFileUploaded(
    user: ExternalUser,
    file: UploadedFile
  )(using hc: HeaderCarrier): Future[AuditResult]

  def auditMultipleFilesUploaded(
    user:  ExternalUser,
    files: Seq[UploadedFile]
  )(using hc: HeaderCarrier): Future[AuditResult]
}
