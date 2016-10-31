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

package uk.gov.hmrc.agentclientauthorisation.controllers

import org.mockito.Mockito.when
import play.api.libs.json.JsArray
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.agentclientauthorisation.connectors.{AgenciesFakeConnector, AuthConnector}
import uk.gov.hmrc.agentclientauthorisation.model.{Invitation, MtdClientId}
import uk.gov.hmrc.agentclientauthorisation.repository.InvitationsRepository
import uk.gov.hmrc.agentclientauthorisation.service.PostcodeService
import uk.gov.hmrc.agentclientauthorisation.support.{AuthMocking, ResettingMockitoSugar}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class InvitationsControllerSpec extends UnitSpec with ResettingMockitoSugar with AuthMocking {
  val invitationsRepository = resettingMock[InvitationsRepository]
  val postcodeService = resettingMock[PostcodeService]
  val authConnector = resettingMock[AuthConnector]
  val agenciesFakeConnector = resettingMock[AgenciesFakeConnector]

  "getInvitationsForClient" should {

    "return 200 and an empty list when there are no invitations for the client" in {
      val controller = new InvitationsController(invitationsRepository, postcodeService, authConnector, agenciesFakeConnector)

      val clientId = MtdClientId("MTD-SA-12345")
      mockMtdSubscriberAuth(clientId)
      when(invitationsRepository.list("mtd-sa", clientId.value)).thenReturn(Future successful Nil)

      val request = FakeRequest()

      val result: Result = await(controller.getInvitationsForClient(clientId.value)(request))
      status(result) shouldBe 200

      (jsonBodyOf(result) \ "_embedded" \ "invitations") shouldBe JsArray()
    }

  }
}
