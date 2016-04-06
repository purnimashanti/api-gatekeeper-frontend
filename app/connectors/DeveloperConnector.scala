/*
 * Copyright 2016 HM Revenue & Customs
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

package connectors

import config.WSHttp
import connectors.AuthConnector._
import model.UserResponse
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}

object DeveloperConnector extends DeveloperConnector {
  override val developerBaseUrl: String = s"${baseUrl("third-party-developer")}"
  override val http = WSHttp
}

trait DeveloperConnector {
  val developerBaseUrl: String
  val http: HttpPost with HttpGet

  def fetchByEmail(email: String)(implicit hc: HeaderCarrier) = {
    http.GET[UserResponse](s"$developerBaseUrl/developer?email=$email")
  }

  def fetchByEmails(emails: Seq[String])(implicit hc: HeaderCarrier) = {
    http.GET[Seq[UserResponse]](s"$developerBaseUrl/developers/emails=${emails.mkString(",")}")
  }
}
