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

import cats.data.{NonEmptyList, Validated}
import cats.implicits.catsSyntaxTuple5Semigroupal
import models.RecipientDetails
import models.sdec.AccessValidationFailure.*
import play.api.libs.json.{JsString, Writes}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.retrieve.Name
import utils.SuccessfulResult

enum AccessValidationFailure {
  case InsufficientConfidence
  case EmailMismatch
  // The above two are probably the only fields we care about the three below can potentially be removed
  case AffinityMismatch
  case NinoMismatch
  case NameMismatch
}

object AccessValidationFailure {
  given Writes[AccessValidationFailure] = Writes(failure => JsString(failure.toString))
}

object IdentityValidationPolicy {

  def validate(
    user:      ExternalUser,
    recipient: RecipientDetails
  ): Either[List[AccessValidationFailure], SuccessfulResult.type] = {

    def matches(a: Option[String], b: Option[String]): Boolean =
      a.map(_.toLowerCase.trim) == b.map(_.toLowerCase.trim)

    def fullNameMatches(fullName: Option[Name], recipient: RecipientDetails): Boolean =
      matches(
        fullName.flatMap(fn => fn.name.map(first => s"$first ${fn.lastName.getOrElse("")}")),
        Some(recipient.firstName.trim + " " + recipient.lastName.trim)
      )

    (
      Validated.condNel(
        user.affinityGroup.contains(AffinityGroup.Individual),
        SuccessfulResult,
        AffinityMismatch
      ),
      Validated.condNel(
        user.confidenceLevel.level >= 250,
        SuccessfulResult,
        InsufficientConfidence
      ),
      Validated.condNel(
        matches(user.email, Some(recipient.email)),
        SuccessfulResult,
        EmailMismatch
      ),
      Validated.condNel(
        matches(user.nino, Some(recipient.nationalInsuranceNumber)),
        SuccessfulResult,
        NinoMismatch
      ),
      Validated.condNel(
        fullNameMatches(user.name, recipient),
        SuccessfulResult,
        NameMismatch
      )
    ).mapN((_, _, _, _, _) => SuccessfulResult) match {
      case Validated.Valid(_)   => Right(SuccessfulResult)
      case Validated.Invalid(e) => Left(e.toList)
    }
  }

}
