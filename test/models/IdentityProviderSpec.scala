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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class IdentityProviderSpec extends AnyFreeSpec with Matchers {

  "IdentityProvider.fromProviderType" - {

    "resolves the Government Gateway string" in {
      IdentityProvider.fromProviderType("GovernmentGateway") mustBe
        Some(IdentityProvider.GovernmentGateway)
    }

    "resolves the One Login string" in {
      IdentityProvider.fromProviderType("ONE_LOGIN") mustBe
        Some(IdentityProvider.OneLogin)
    }

    "returns None for an unknown string" in {
      IdentityProvider.fromProviderType("unknown-provider") mustBe None
    }
  }

  "IdentityProvider JSON format" - {

    "writes a provider as its providerType string" in {
      Json.toJson(IdentityProvider.OneLogin) mustBe JsString("ONE_LOGIN")
    }

    "reads a known string back into a provider" in {
      JsString("GovernmentGateway").validate[IdentityProvider] mustBe
        JsSuccess(IdentityProvider.GovernmentGateway)
    }

    "fails to read an unknown string" in {
      JsString("unknown-provider").validate[IdentityProvider] mustBe a[JsError]
    }
  }
}
