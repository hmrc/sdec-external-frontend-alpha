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

package models

import play.api.libs.json.*

enum ThreadStatus:
    case Draft, Active, Closed, Archived

object ThreadStatus:

    given Format[ThreadStatus] = Format(
      Reads {
        case JsString(value) =>
          ThreadStatus.values
            .find(_.toString == value)
            .map(JsSuccess(_))
            .getOrElse(JsError(s"Unknown ThreadStatus: $value"))

        case _ =>
          JsError("Expected JSON string")
      },
      Writes(status => JsString(status.toString))
    )
