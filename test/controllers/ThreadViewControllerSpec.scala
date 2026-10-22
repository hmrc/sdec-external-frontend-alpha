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

import base.SpecBase
import controllers.actions.*
import fixtures.{IdentifyExternalUserFixtures, ThreadReferenceFixtures}
import models.*
import org.jsoup.Jsoup
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import service.ThreadReferenceServiceAlgebra
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

class ThreadViewControllerSpec extends SpecBase with IdentifyExternalUserFixtures with ThreadReferenceFixtures {

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = ExecutionContext.global

  private def serviceReturning(result: Future[ThreadReference]) =
    new ThreadReferenceServiceAlgebra {
      override def checkThreadReference(threadReferenceStr: String)(using
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[ThreadReference] = result
    }

  private def fullAccessApplicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[IdentifyExternalUser].toInstance(
          identifyExternalUserWith(ConfidenceLevel.L250)(using ExecutionContext.global)
        ),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(None))
      )

  private def request = FakeRequest(GET, routes.ThreadViewController.onPageLoad(threadId).url)

  "ThreadViewController" - {
    "onPageLoad" - {
      "must return OK and render the full thread view when access is granted" in {
        val application =
          fullAccessApplicationBuilder
            .overrides(
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(serviceReturning(Future.successful(threadWith(matchingRecipient))))
            )
            .build()

        running(application) {
          val result = route(application, request).value
          status(result) mustBe OK

          val document = Jsoup.parse(contentAsString(result))
          document.select("h1").text() mustBe "John Doe"
        }
      }

      "must return OK and render the limited interruption view when access is limited" in {
        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(serviceReturning(Future.successful(threadWith(matchingRecipient))))
            )
            .build()

        running(application) {
          val result = route(application, request).value

          status(result) mustBe OK

          val document = Jsoup.parse(contentAsString(result))
          document.select("h1").text() mustBe "Action required for full access"
          document.select(".govuk-caption-l").text() must include("Share Files Securely with HMRC")
        }
      }

      "must redirect to journey recovery when access is denied" in {
        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(serviceReturning(Future.successful(threadWith(mismatchedRecipient))))
            )
            .build()

        running(application) {
          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to journey recovery when the thread is not found" in {
        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(serviceReturning(Future.failed(new NotFoundException("not found"))))
            )
            .build()

        running(application) {
          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
