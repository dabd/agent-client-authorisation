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

package uk.gov.hmrc.agentclientauthorisation.model

import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.play.test.UnitSpec

class InvitationSpec extends UnitSpec {
  "Dates in the serialised JSON" should {
    "be in ISO8601 format" in {
      val created = "2010-01-01T01:00:23.456Z"
      val lastUpdated = "2010-01-02T04:00:23.456Z"
      val invitation = Invitation(
        id = BSONObjectID.generate,
        arn = Arn("myAgency"),
        regime = "regime",
        clientId = "clientId",
        postcode = "A11 1AA",
        events = List(StatusChangeEvent(DateTime.parse(created), Pending), StatusChangeEvent(DateTime.parse(lastUpdated), Accepted))
      )

      val json = Json.toJson(invitation)

      (json \ "created").as[String] shouldBe created
      (json \ "lastUpdated").as[String] shouldBe lastUpdated
    }
  }
}
