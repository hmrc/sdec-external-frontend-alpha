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
import forms.providers.ThreadReferenceFormProvider
import models.{ThreadReference, ThreadStatus}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import service.ThreadReferenceServiceAlgebra
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import views.html.EnterThreadReferenceView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class EnterThreadReferenceControllerSpec extends SpecBase {

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = ExecutionContext.global

  "EnterThreadReferenceController" - {

    "onPageLoad" - {

      "must return OK" in {

        val application = applicationBuilder().build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              routes.EnterThreadReferenceController
                .onPageLoad()
                .url
            )

          val result = route(application, request).value

          status(result) mustBe OK
        }
      }

      "must render the correct view" in {

        val application = applicationBuilder().build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              routes.EnterThreadReferenceController
                .onPageLoad()
                .url
            )

          val result = route(application, request).value

          application.injector.instanceOf[EnterThreadReferenceView]

          application.injector
            .instanceOf[ThreadReferenceFormProvider]
            .apply()

          val document = Jsoup.parse(contentAsString(result))

          status(result) mustBe OK

          document.title() mustBe "Share Files Securely with HMRC - Share Files Securely with HMRC - GOV.UK"

          document.select("h1").text() mustBe "Enter the thread reference number"

          document.select("input[name=thread-reference]").size() mustBe 1
        }
      }
    }

    "onContinue" - {

      "must return BAD_REQUEST when the form is empty" in {

        val application = applicationBuilder().build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.EnterThreadReferenceController
                .onContinue()
                .url
            ).withFormUrlEncodedBody(
              "thread-reference" -> ""
            )

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
        }
      }

      "must return OK when a valid thread reference is submitted" in {

        val threadReference = models.ThreadReference(
          id = "1",
          threadReference = "ABC123DEF456",
          status = ThreadStatus.Active,
          createdTimeStamp = LocalDateTime.now(),
          lastUpdatedTimeStamp = LocalDateTime.now(),
          threadExpiryDate = LocalDate.now(),
          associatedCaseReference = "CASE123"
        )

        val mockThreadReferenceService = new ThreadReferenceServiceAlgebra {
          override def checkThreadReference(threadReferenceStr: String)(using
              hc: HeaderCarrier,
              ec: ExecutionContext
          ): Future[ThreadReference] =
            Future.successful(threadReference)
        }

        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceFormProvider]
                .toInstance(new ThreadReferenceFormProvider {
                  override def validateThreadReference(
                      reference: String
                  ): Boolean = true
                }),
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(mockThreadReferenceService)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.EnterThreadReferenceController
                .onContinue()
                .url
            ).withFormUrlEncodedBody(
              "thread-reference" -> "ABC123DEF456"
            )

          val result = route(application, request).value

          status(result) mustBe OK
        }
      }

      "must return NOTFOUND when a valid thread reference is submitted but backend does not find it in database" in {

        val mockThreadReferenceService = new ThreadReferenceServiceAlgebra {
          override def checkThreadReference(threadReferenceStr: String)(using
              hc: HeaderCarrier,
              ec: ExecutionContext
          ): Future[ThreadReference] =
            Future.failed(new NotFoundException("not found"))
        }

        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceFormProvider]
                .toInstance(new ThreadReferenceFormProvider {
                  override def validateThreadReference(
                      reference: String
                  ): Boolean = true
                }),
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(mockThreadReferenceService)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.EnterThreadReferenceController
                .onContinue()
                .url
            ).withFormUrlEncodedBody(
              "thread-reference" -> "ABC123DEF456"
            )

          val result = route(application, request).value

          status(result) mustBe NOT_FOUND
        }
      }

      "must return NOTFOUND when a valid thread reference is submitted but gets an UpstreamErrorResponse" in {

        val mockThreadReferenceService = new ThreadReferenceServiceAlgebra {
          override def checkThreadReference(threadReferenceStr: String)(using
              hc: HeaderCarrier,
              ec: ExecutionContext
          ): Future[ThreadReference] =
            Future.failed(
              new UpstreamErrorResponse(
                "Not found",
                Status.NOT_FOUND,
                Status.NOT_FOUND,
                Map()
              )
            )
        }

        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceFormProvider]
                .toInstance(new ThreadReferenceFormProvider {
                  override def validateThreadReference(
                      reference: String
                  ): Boolean = true
                }),
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(mockThreadReferenceService)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.EnterThreadReferenceController
                .onContinue()
                .url
            ).withFormUrlEncodedBody(
              "thread-reference" -> "ABC123DEF456"
            )

          val result = route(application, request).value

          status(result) mustBe NOT_FOUND
        }
      }

      "must return Server Error when a valid thread reference is submitted but gets an Any other errors" in {

        val mockThreadReferenceService = new ThreadReferenceServiceAlgebra {
          override def checkThreadReference(threadReferenceStr: String)(using
              hc: HeaderCarrier,
              ec: ExecutionContext
          ): Future[ThreadReference] =
            Future.failed(
              new UpstreamErrorResponse(
                "There was a problem",
                Status.INTERNAL_SERVER_ERROR,
                Status.INTERNAL_SERVER_ERROR,
                Map()
              )
            )
        }

        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceFormProvider]
                .toInstance(new ThreadReferenceFormProvider {
                  override def validateThreadReference(
                      reference: String
                  ): Boolean = true
                }),
              bind[ThreadReferenceServiceAlgebra]
                .toInstance(mockThreadReferenceService)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.EnterThreadReferenceController
                .onContinue()
                .url
            ).withFormUrlEncodedBody(
              "thread-reference" -> "ABC123DEF456"
            )

          val result = route(application, request).value

          status(result) mustBe SERVICE_UNAVAILABLE
        }
      }

      "must return BAD_REQUEST when business validation fails" in {

        val application =
          applicationBuilder()
            .overrides(
              bind[ThreadReferenceFormProvider]
                .toInstance(new ThreadReferenceFormProvider {
                  override def validateThreadReference(
                      reference: String
                  ): Boolean = false
                })
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              routes.EnterThreadReferenceController
                .onContinue()
                .url
            ).withFormUrlEncodedBody(
              "thread-reference" -> "ABC123DEF456"
            )

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
}
