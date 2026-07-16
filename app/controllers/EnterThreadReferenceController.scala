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

import controllers.actions.IdentifierAction
import forms.models.ThreadReferenceForm
import forms.providers.ThreadReferenceFormProvider
import models.Mode
import play.api.Logging
import play.api.data.Form
import play.api.http.Status as HttpStatus
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.*
import service.ThreadReferenceServiceAlgebra
import uk.gov.hmrc.http.{NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{EnterThreadReferenceView, ThreadReferenceView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterThreadReferenceController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    enterThreadReferenceView: EnterThreadReferenceView,
    formProvider: ThreadReferenceFormProvider,
    threadReferenceView: ThreadReferenceView,
    threadReferenceService: ThreadReferenceServiceAlgebra
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form: Form[ThreadReferenceForm] = formProvider()

  def onPageLoad(
      mode: Mode,
      threadReferenceForm: Form[ThreadReferenceForm] = form
  ): Action[AnyContent] = identify { implicit request =>
    Ok(enterThreadReferenceView(threadReferenceForm, mode))
  }

  def onContinue(mode: Mode): Action[AnyContent] =
    identify.async { implicit request =>
      val formData = form.bindFromRequest()
      formData.value
        .filter(t => formProvider.validateThreadReference(t.reference))
        .fold(
          Future.successful(returnBadRequest(formData, mode))
        ) { tr =>
          getThreadInformation(formData, mode, tr)
        }
    }

  private def getThreadInformation(
      form: Form[ThreadReferenceForm],
      mode: Mode,
      trForm: ThreadReferenceForm
  )(using Request[?]): Future[Result] =
    threadReferenceService
      .checkThreadReference(trForm.reference)
      .map { thread =>
        Ok(threadReferenceView(mode, ThreadReferenceForm(thread.threadReference)))
      }
      .recover {
        case _: NotFoundException =>
          val formWithError =
            form.withGlobalError(Messages("sdec.enterthreadref.api.notfound"))
          logger.warn(s"Thread Reference Not found: ${trForm.reference}")
          NotFound(enterThreadReferenceView(formWithError, mode))
        case e: UpstreamErrorResponse if e.statusCode == HttpStatus.NOT_FOUND =>
          logger.warn(s"Thread Reference Not found: ${trForm.reference}")
          val formWithError =
            form.withGlobalError(Messages("sdec.enterthreadref.api.notfound"))
          NotFound(enterThreadReferenceView(formWithError, mode))
        case ex =>
          val formWithError =
            form.withGlobalError(Messages("sdec.enterthreadref.api.error"))
          logger.error("Failed to retrieve thread information", ex)
          ServiceUnavailable(enterThreadReferenceView(formWithError, mode))
      }

  private def returnBadRequest(form: Form[ThreadReferenceForm], mode: Mode)(using
      request: Request[?]
  ): Result = {
    logger.warn(s"Returning bad request for ${form.value}")
    val formWithError =
      form.withGlobalError(Messages("sdec.enterthreadref.error.problem.message"))
    BadRequest(enterThreadReferenceView(formWithError, mode))
  }

}
