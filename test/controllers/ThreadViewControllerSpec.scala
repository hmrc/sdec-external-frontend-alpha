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
import models.{ThreadReference, ThreadStatus}
import org.jsoup.Jsoup
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

  private val threadId = "THREAD1000AA"

  private val thread = ThreadReference(
    id = threadId,
    recipientName = Some("Jenny Worthy"),
    message = Some("message"),
    status = ThreadStatus.Active,
    createdTimeStamp = Some(LocalDateTime.now()),
    lastUpdatedTimeStamp = LocalDateTime.now(),
    threadExpiryDate = LocalDate.now().plusDays(28),
    associatedCaseReference = "CASE-001"
  )

  private def serviceReturning(result: Future[ThreadReference]) =
    new ThreadReferenceServiceAlgebra {
      override def checkThreadReference(threadReferenceStr: String)(using
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[ThreadReference] = result
    }

  private def request = FakeRequest(GET, routes.ThreadViewController.onPageLoad(threadId).url)

  "ThreadViewController" - {

    "onPageLoad" - {

      "must return OK and render the thread details" in {

        val application =
          applicationBuilder()
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
          document.select(".govuk-caption-l").text() must include(threadId)
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
