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

package uk.gov.hmrc.agentclientauthorisation.sa.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.Future

class CesaIndividualsConnector(http: HttpGet, cesaBaseUrl: String) {
  implicit val readsCesaDesignatoryDetailsName = Json.reads[CesaDesignatoryDetailsName]
  implicit val readsCesaDesignatoryDetailsAddress = Json.reads[CesaDesignatoryDetailsAddress]
  implicit val readsCesaTaxpayer = Json.reads[CesaTaxpayer]

  def url(path: String) = s"$cesaBaseUrl$path"

  def taxpayer(utr: SaUtr)(implicit hc: HeaderCarrier): Future[Option[CesaTaxpayer]] =
    http.GET[Option[CesaTaxpayer]](url(s"/self-assessment/individual/$utr/designatory-details/taxpayer"))
}

case class CesaDesignatoryDetailsName(
  title: Option[String],
  forename: Option[String],
  surname: Option[String]
)

case class CesaDesignatoryDetailsAddress(
  postcode: Option[String]
)

case class CesaTaxpayer(name: CesaDesignatoryDetailsName, address: CesaDesignatoryDetailsAddress)
