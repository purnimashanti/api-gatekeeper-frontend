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

package unit.controllers

import model._
import org.scalatestplus.play.PlaySpec
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest

class DeveloperViewSpec extends PlaySpec {

  "developersView" must {
    val users = Seq(
      User("sample@email.com", "Sample", "Email", Some(false)),
      User("another@email.com", "Sample2", "Email", Some(true)),
      UnregisteredCollaborator("something@email.com"))

    "list all developers" in new App {
      implicit val fakeRequest = FakeRequest
      val result = views.html.developers.developers.render(PageableCollection(users, 1, 10), "", Map.empty, None, None, FakeRequest(), None, applicationMessages)
      result.contentType must include( "text/html" )
      users.foreach(user => result.body must include(user.email))
    }
  }
}
