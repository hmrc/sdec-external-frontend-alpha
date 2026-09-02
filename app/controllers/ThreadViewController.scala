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

package controllers

import controllers.actions.IdentifyExternalUser
import play.api.Logging
import play.api.http.Status as HttpStatus
import play.api.i18n.I18nSupport
import play.api.mvc.*
import service.ThreadReferenceServiceAlgebra
import uk.gov.hmrc.http.{NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThreadView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ThreadViewController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identifyExternalUser:     IdentifyExternalUser,
  threadReferenceService:   ThreadReferenceServiceAlgebra,
  threadView:               ThreadView
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(threadId: String): Action[AnyContent] =
    identifyExternalUser.async { request =>
      given Request[AnyContent] = request

      threadReferenceService
        .checkThreadReference(threadId)
        .map(thread => Ok(threadView(thread)))
        .recover {
          case _: NotFoundException =>
            logger.warn(s"Thread $threadId not found")
            Redirect(routes.JourneyRecoveryController.onPageLoad())

          case e: UpstreamErrorResponse if e.statusCode == HttpStatus.NOT_FOUND =>
            logger.warn(s"Thread $threadId not found")
            Redirect(routes.JourneyRecoveryController.onPageLoad())

          case ex =>
            logger.error(s"Failed to retrieve thread $threadId", ex)
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
    }
}
