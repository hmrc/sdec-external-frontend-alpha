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
import models.{ThreadReference, ThreadStatus, UserAnswers}
import org.jsoup.Jsoup
import pages.ThreadReferencePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import service.ThreadReferenceServiceAlgebra
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class ThreadViewControllerSpec extends SpecBase {

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = ExecutionContext.global

  private val thread = ThreadReference(
    id = "THREAD1000AA",
    recipientName = Some("Jenny Worthy"),
    message = Some("Dear Jenny\n\nPlease reply."),
    status = ThreadStatus.Active,
    createdTimeStamp = Some(LocalDateTime.now()),
    lastUpdatedTimeStamp = LocalDateTime.now(),
    threadExpiryDate = LocalDate.now().plusDays(28),
    associatedCaseReference = "CASE-001"
  )

  private val userAnswers: UserAnswers =
    emptyUserAnswers.set(ThreadReferencePage, "THREAD1000AA").success.value

  private def serviceReturning(result: Future[ThreadReference]) =
    new ThreadReferenceServiceAlgebra {
      override def checkThreadReference(threadReferenceStr: String)(using
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[ThreadReference] = result
    }

  private def request = FakeRequest(GET, routes.ThreadViewController.onPageLoad().url)

  "ThreadViewController" - {

    "onPageLoad" - {

      "must return OK and render the thread details" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(serviceReturning(Future.successful(thread)))
            )
            .build()

        running(application) {

          val result = route(application, request).value

          status(result) mustBe OK

          val document = Jsoup.parse(contentAsString(result))

          document.select("h1").text() mustBe "Jenny Worthy"
          document.select(".govuk-caption-l").text() must include("THREAD1000AA")
        }
      }

      "must redirect to journey recovery when there is no thread reference in session" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to journey recovery when the thread is not found" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
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
