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

import controllers.actions.IdentifyExternalUser
import models.requests.ExternalUserRequest
import play.api.mvc.*
import play.api.test.Helpers.stubBodyParser
import uk.gov.hmrc.auth.core.ConfidenceLevel

import scala.concurrent.{ExecutionContext, Future}

trait IdentifyExternalUserFixtures extends ExternalUserFixtures {

  def identifyExternalUserWith(confidenceLevel: ConfidenceLevel)(using ec: ExecutionContext): IdentifyExternalUser =
    new IdentifyExternalUser {
      override val parser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty)

      override def executionContext: ExecutionContext = ec

      override def invokeBlock[A](
        request: Request[A],
        block:   ExternalUserRequest[A] => Future[Result]
      ): Future[Result] =
        block(ExternalUserRequest(request, externalUser.copy(confidenceLevel = confidenceLevel)))
    }
}
