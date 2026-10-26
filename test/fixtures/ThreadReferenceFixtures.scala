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

import models.{RecipientDetails, ThreadDetails, ThreadReference, ThreadStatus}

import java.time.{LocalDate, LocalDateTime}

trait ThreadReferenceFixtures {

  val threadId: String = "THREAD1000AA"

  val mismatchedRecipient: RecipientDetails = RecipientDetails(
    firstName = "John",
    lastName = "Smith",
    email = "some@example.com",
    phoneNumber = "07123456789",
    nationalInsuranceNumber = "QQQQQQQQC",
    hasRelatedCase = false,
    caseReferenceNumber = None
  )

  val matchingRecipient: RecipientDetails = RecipientDetails(
    firstName = "John",
    lastName = "Doe",
    email = "johndoe@example.com",
    phoneNumber = "07123456789",
    nationalInsuranceNumber = "AA000000A",
    hasRelatedCase = false,
    caseReferenceNumber = None
  )

  def threadWith(recipientDetails: RecipientDetails): ThreadReference =
    ThreadReference(
      id = threadId,
      status = ThreadStatus.Active,
      createdTimeStamp = LocalDateTime.now().minusDays(2),
      lastUpdatedTimeStamp = LocalDateTime.now().minusHours(3),
      threadExpiryDate = LocalDate.now().plusDays(28),
      associatedCaseReference = "CASE-001",
      recipientDetails = recipientDetails,
      threadDetails = ThreadDetails(
        message = "some message",
        responseDate = LocalDate.now().plusDays(7)
      )
    )
}
