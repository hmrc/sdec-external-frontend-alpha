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

import models.sdec.AccessValidationFailure.EmailMismatch
import play.api.libs.json.{JsString, Writes}

enum AccessLevel {
  case NoAccess
  case Limited
  case Full
}

object AccessLevel {
  given Writes[AccessLevel] = Writes(level => JsString(level.toString))

  def fromAccessValidationFailures(failures: List[AccessValidationFailure]): AccessLevel = {
    val (isLowConfidence, otherFailures) =
      failures.partition(failure => failure == AccessValidationFailure.InsufficientConfidence)
    val isEmailMismatch = otherFailures.contains(EmailMismatch)
    if isLowConfidence.nonEmpty && !isEmailMismatch then Limited
    else NoAccess
  }
}
