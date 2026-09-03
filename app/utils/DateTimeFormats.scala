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

package utils

import play.api.i18n.Lang

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormats {

  // 3 December 2011
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  // 3 December 2011 at 12:22pm
  private val dateAndTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma")

  private val localisedDateTimeFormatters = Map(
    "en" -> dateFormatter,
    "cy" -> dateFormatter.withLocale(new Locale("cy"))
  )

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedDateTimeFormatters.getOrElse(lang.code, dateFormatter)

  extension (localDateTime: LocalDateTime) {
    def toDateAndTimeFormat: String = localDateTime.format(dateAndTimeFormatter)
  }
}
