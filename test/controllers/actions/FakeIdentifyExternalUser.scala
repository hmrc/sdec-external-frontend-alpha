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

package controllers.actions

import com.google.inject.Inject
import models.requests.ExternalUserRequest
import models.sdec.{CustomerIdentifier, ExternalUser, IdentityProvider}
import play.api.mvc.*
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifyExternalUser @Inject() (bodyParsers: BodyParsers.Default)(implicit
  val executionContext: ExecutionContext
) extends IdentifyExternalUser {

  override val parser: BodyParser[AnyContent] = bodyParsers

  private val stubUser: ExternalUser = ExternalUser(
    id = CustomerIdentifier(IdentityProvider.OneLogin, "stub-sub"),
    email = Some("johndoe@example.com"),
    nino = Some("AA000000A"),
    userDetails = None,
    enrolments = Enrolments(Set.empty)
  )

  override def invokeBlock[A](
    request: Request[A],
    block:   ExternalUserRequest[A] => Future[Result]
  ): Future[Result] =
    block(ExternalUserRequest(request, stubUser))
}
