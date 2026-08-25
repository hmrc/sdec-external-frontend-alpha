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

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.requests.ExternalUserRequest
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IdentifyExternalUserActionSpec extends SpecBase {

  given ec: ExecutionContext = ExecutionContext.global

  class Harness(action: IdentifyExternalUser) {
    def onPageLoad(): Action[AnyContent] =
      action { (r: ExternalUserRequest[?]) =>
        Results.Ok(r.externalUser.id.providerId)
      }
  }

  "IdentifyExternalUserAction" - {

    "identifies a One Login user and runs the block" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val retrievals =
          new ~(
            new ~(
              new ~(
                Option(Credentials("sub-123", "ONE_LOGIN")),
                Option("johndoe@example.com")
              ),
              Option("AA000000A")
            ),
            Enrolments(Set.empty)
          )

        val action = new IdentifyExternalUserAction(
          new FakeSuccessAuthConnector(retrievals),
          appConfig,
          bodyParsers
        )

        val result = new Harness(action).onPageLoad()(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe "sub-123"
      }
    }

    "identifies a Government Gateway user and runs the block" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val retrievals =
          new ~(
            new ~(
              new ~(
                Option(Credentials("cred-123", "GovernmentGateway")),
                Option("gg@example.com")
              ),
              Option("AA000000A")
            ),
            Enrolments(Set.empty)
          )

        val action = new IdentifyExternalUserAction(
          new FakeSuccessAuthConnector(retrievals),
          appConfig,
          bodyParsers
        )

        val result = new Harness(action).onPageLoad()(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe "cred-123"
      }
    }

    "redirects to log in when there is no active session" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val action = new IdentifyExternalUserAction(
          new FakeFailingAuthConnector(new MissingBearerToken),
          appConfig,
          bodyParsers
        )

        val result = new Harness(action).onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "redirects to the unauthorised page when the user is not authorised" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val action = new IdentifyExternalUserAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          appConfig,
          bodyParsers
        )

        val result = new Harness(action).onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController
          .onPageLoad()
          .url
      }
    }

    "redirects to the unauthorised page when the provider type is unrecognised" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        val retrievals =
          new ~(
            new ~(
              new ~(
                Option(Credentials("id-1", "SomethingElse")),
                Option.empty[String]
              ),
              Option.empty[String]
            ),
            Enrolments(Set.empty)
          )

        val action = new IdentifyExternalUserAction(
          new FakeSuccessAuthConnector(retrievals),
          appConfig,
          bodyParsers
        )

        val result = new Harness(action).onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController
          .onPageLoad()
          .url
      }
    }
  }
}

class FakeSuccessAuthConnector(result: Any) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.successful(result.asInstanceOf[A])
}
