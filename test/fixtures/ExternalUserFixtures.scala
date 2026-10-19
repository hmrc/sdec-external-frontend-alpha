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

package fixtures

import models.sdec.{CustomerIdentifier, ExternalUser, IdentityProvider}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.ConfidenceLevel.L200
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.auth.core.retrieve.Name

trait ExternalUserFixtures {

  val customerIdentifier: CustomerIdentifier =
    CustomerIdentifier(IdentityProvider.OneLogin, "stub-sub")

  val externalUser: ExternalUser = ExternalUser(
    id = customerIdentifier,
    name = Some(Name(Some("John"), Some("Doe"))),
    email = Some("johndoe@example.com"),
    nino = Some("AA000000A"),
    enrolments = Enrolments(Set.empty),
    affinityGroup = Some(Individual),
    confidenceLevel = L200
  )
  
}
