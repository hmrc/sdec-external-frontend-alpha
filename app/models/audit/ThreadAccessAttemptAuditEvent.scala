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

import models.sdec.{AccessValidationFailure, CustomerIdentifier}
import play.api.libs.json.{JsObject, Json, OWrites, Writes}

final case class ThreadAccessAttemptAuditEvent(
  customerIdentifier: CustomerIdentifier,
  affinityGroup:      Option[String],
  firstName:          Option[String],
  lastName:           Option[String],
  email:              Option[String],
  nino:               Option[String],
  confidenceLevel:    Int,
  threadReference:    String,
  outcome:            ThreadAccessOutcome,
  failureReasons:     List[AccessValidationFailure]
) extends AuditEvent {
  override def auditType: String   = "ThreadAccessAttempt"
  override def detail:    JsObject = Json.toJsObject(this)
}

object ThreadAccessAttemptAuditEvent {
  given writes: OWrites[ThreadAccessAttemptAuditEvent] = Json.writes[ThreadAccessAttemptAuditEvent]
}
