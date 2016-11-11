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

package uk.gov.hmrc.agentclientauthorisation.support

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.OneServerPerSuite
import play.api.{Application, ApplicationLoader, Environment, Mode}
import uk.gov.hmrc.agentclientauthorisation.MicroserviceComponents
import uk.gov.hmrc.mongo.{MongoSpecSupport, Awaiting => MongoAwaiting}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait AppAndStubs extends StartAndStopWireMock with StubUtils with OneServerPerSuite {
  me: Suite =>

  implicit val hc = HeaderCarrier()

  override implicit lazy val app: Application = new MicroserviceComponents(context).application

  //TODO lazy val?
  private def context: ApplicationLoader.Context = {
    val classLoader = ApplicationLoader.getClass.getClassLoader
    val env = new Environment(new java.io.File("."), classLoader, Mode.Test)
    ApplicationLoader.createContext(env, initialSettings = additionalConfiguration
      //TODO delete if not needed, copied from file-upload
//      Map(
//      "mongodb.uri" -> s"mongodb://localhost:27017/$databaseName",
//      "auditing.enabled" -> "false",
//      "feature.basicAuthEnabled" -> "true")
    )
  }

  protected def additionalConfiguration: Map[String, AnyRef] = {
    Map(
      "microservice.services.auth.host" -> wiremockHost,
      "microservice.services.auth.port" -> int2Integer(wiremockPort),
      "microservice.services.agencies-fake.host" -> wiremockHost,
      "microservice.services.agencies-fake.port" -> int2Integer(wiremockPort),
      "microservice.services.relationships.host" -> wiremockHost,
      "microservice.services.relationships.port" -> int2Integer(wiremockPort),
      "microservice.services.cesa.host" -> wiremockHost,
      "microservice.services.cesa.port" -> int2Integer(wiremockPort)
    )
  }
}

trait MongoAppAndStubs extends AppAndStubs with MongoSpecSupport with ResetMongoBeforeTest {
  me: Suite =>

  override protected def additionalConfiguration =
    super.additionalConfiguration + ("mongodb.uri" -> mongoUri)
}

trait ResetMongoBeforeTest extends BeforeAndAfterEach {
  me: Suite with MongoSpecSupport =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    dropMongoDb()
  }

  def dropMongoDb()(implicit ec: ExecutionContext = scala.concurrent.ExecutionContext.global): Unit = {
    Awaiting.await(mongo().drop())
  }
}

object Awaiting extends MongoAwaiting
