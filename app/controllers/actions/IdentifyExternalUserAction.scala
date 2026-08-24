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
import config.FrontendAppConfig
import controllers.routes
import models.requests.ExternalUserRequest
import models.sdec.{CustomerIdentifier, ExternalUser, IdentityProvider}
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifyExternalUser extends ActionBuilder[ExternalUserRequest, AnyContent] with ActionFunction[Request, ExternalUserRequest]

class IdentifyExternalUserAction @Inject() (
  override val authConnector: AuthConnector,
  config:                     FrontendAppConfig,
  val parser:                 BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifyExternalUser
    with AuthorisedFunctions
    with Logging {

  override def invokeBlock[A](
    request: Request[A],
    block:   ExternalUserRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(
        Retrievals.credentials and
          Retrievals.email and
          Retrievals.nino and
          Retrievals.authorisedEnrolments
      ) { case credentials ~ email ~ nino ~ enrolments =>
        val maybeIdentity: Option[CustomerIdentifier] =
          credentials.flatMap { c =>
            IdentityProvider.fromProviderType(c.providerType) match {
              case Some(provider) =>
                Some(CustomerIdentifier(provider, c.providerId))
              case None =>
                logger.warn(
                  s"Unmapped identity provider from auth: ${c.providerType}"
                )
                None
            }
          }

        maybeIdentity
          .map { id =>
            val user = ExternalUser(
              id = id,
              email = email,
              nino = nino,
              userDetails = None,
              enrolments = enrolments
            )
            block(ExternalUserRequest(request, user))
          }
          .getOrElse(
            Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
          )
      }
      .recover {
        case _: NoActiveSession =>
          Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
        case _: AuthorisationException =>
          Redirect(routes.UnauthorisedController.onPageLoad())
      }
  }
}
