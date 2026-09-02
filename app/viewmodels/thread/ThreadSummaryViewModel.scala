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

package viewmodels.thread

import models.{ThreadReference, ThreadStatus}
import play.api.i18n.Messages
import utils.DateTimeFormats.toDateAndTimeFormat

import java.time.LocalDateTime

object ThreadSummaryViewModel {

  def formatDateTime(value: LocalDateTime): String = value.toDateAndTimeFormat

  private def orUnavailable(value: Option[String])(using messages: Messages): String =
    value.map(_.trim).filter(_.nonEmpty).getOrElse(messages("threadview.name.unavailable"))

  def referenceOrUnavailable(thread: ThreadReference)(using messages: Messages): String =
    orUnavailable(Option(thread.id))

  def nameOrUnavailable(thread: ThreadReference)(using messages: Messages): String =
    orUnavailable(thread.recipientName)

  def insetKey(thread: ThreadReference): String =
    thread.status match {
      case ThreadStatus.Draft    => "threadview.inset.draft"
      case ThreadStatus.Active   => "threadview.inset.awaitingReply"
      case ThreadStatus.Closed   => "threadview.inset.closed"
      case ThreadStatus.Archived => "threadview.inset.archived"
    }

  def canReply(thread: ThreadReference): Boolean =
    thread.status == ThreadStatus.Active
}
