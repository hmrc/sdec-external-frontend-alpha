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

package models.sdec

import play.api.libs.json.*

enum IdentityProvider(val providerType: String) {
  case GovernmentGateway extends IdentityProvider("GovernmentGateway")
  case OneLogin extends IdentityProvider("ONE_LOGIN")
}

object IdentityProvider {

  def fromProviderType(providerType: String): Option[IdentityProvider] =
    values.find(_.providerType == providerType)

  given Format[IdentityProvider] =
    Format(
      Reads(json =>
        json.validate[String].flatMap { s =>
          fromProviderType(s)
            .map(JsSuccess(_))
            .getOrElse(JsError(s"Unknown Identity Provider: $s"))
        }
      ),
      Writes(p => JsString(p.providerType))
    )
}
