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

import models.RecipientDetails
import models.sdec.AccessValidationFailure.*
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.retrieve.Name

sealed trait AccessValidationFailure
object AccessValidationFailure {
  case object AffinityMismatch extends AccessValidationFailure
  case object InsufficientConfidence extends AccessValidationFailure
  case object EmailMismatch extends AccessValidationFailure
  case object NinoMismatch extends AccessValidationFailure
  case object NameMismatch extends AccessValidationFailure
}

object IdentityValidationPolicy {

  def validate(
    user:      ExternalUser,
    recipient: RecipientDetails
  ): Either[AccessValidationFailure, Unit] =

    for {
      _ <- check(user.affinityGroup.contains(AffinityGroup.Individual), AffinityMismatch)
      _ <- check(user.confidenceLevel.level >= 200, InsufficientConfidence)
      _ <- check(matches(user.email, Some(recipient.email)), EmailMismatch)
      _ <- check(matches(user.nino, Some(recipient.nationalInsuranceNumber)), NinoMismatch)
      _ <- check(nameMatches(user.name, recipient), NameMismatch)
    } yield ()

  private def check(cond: Boolean, failure: AccessValidationFailure): Either[AccessValidationFailure, Unit] =
    if cond then Right(()) else Left(failure)

  private def matches(a: Option[String], b: Option[String]): Boolean =
    (a, b) match {
      case (Some(x), Some(y)) => x.equalsIgnoreCase(y.trim)
      case _                  => false
    }

  private def nameMatches(name: Option[Name], recipient: RecipientDetails): Boolean =
    matches(name.flatMap(_.name), Some(s"${recipient.firstName} ${recipient.lastName}"))
}
