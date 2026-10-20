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

import cats.kernel.CommutativeGroup

/** This is a replacement for `Unit` that the compiler won't auto-generate. Safer. */
case object SuccessfulResult {
  type SuccessfulResult = SuccessfulResult.type

  /** Implements addition/subtraction for [[SuccessfulResult]], to tell Cats that multiple references can be combined.
    *
    * Makes it easier to use [[SuccessfulResult]] with `cats.data.Validated`, `cats.Traverse` etc. Similar to the
    * built-in implementation for `Unit`.
    */
  implicit object SuccessfulResultGroup extends CommutativeGroup[SuccessfulResult] {
    def empty:                                             SuccessfulResult = SuccessfulResult
    def combine(x: SuccessfulResult, y: SuccessfulResult): SuccessfulResult = SuccessfulResult
    def inverse(a: SuccessfulResult):                      SuccessfulResult = SuccessfulResult
  }

}
