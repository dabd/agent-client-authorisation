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

package uk.gov.hmrc.agentclientauthorisation.controllers.api

import play.api.libs.json.JsValue
import play.utils.UriEncoding
import uk.gov.hmrc.agentclientauthorisation.controllers.api.ApiTestSupport.Endpoint
import uk.gov.hmrc.agentclientauthorisation.support.Resource
import uk.gov.hmrc.play.http.HttpResponse

import scala.util.Try
import scala.xml.{Elem, XML}

object ApiTestSupport {

  case class Endpoint(uriPattern: String,
                      endPointName: String,
                      version: String
                     )

}

trait ApiTestSupport {

  val runningPort: Int

  private val definitionPath = "/api/definition"
  private val xmlDocumentationPath = "/api/documentation"
  private val ramlPath = "/api/conf"

  private def definitionsJson = new Resource(definitionPath.toString, runningPort).get().json

  private val DefinitionsFileApiSection = (definitionsJson \ "api").as[JsValue]
  private val DefinitionsFileApiVersions: List[JsValue] = (DefinitionsFileApiSection \ "versions").as[List[JsValue]]

  def xmlDocumentationFor(endpoint: Endpoint): (Int, Try[Elem]) = {
    val endpointPath = s"${endpoint version}/${UriEncoding.encodePathSegment(endpoint.endPointName, "UTF-8")}"
    val response: HttpResponse = new Resource(s"$xmlDocumentationPath/$endpointPath", runningPort).get()
    (response.status, Try(XML.loadString(response.body)))
  }

  def endpointsByVersion(api: JsValue) : (String, List[Endpoint]) =
     (api \ "version").as[String] -> getEndpoints(api)

  def ramlByVersion(api: JsValue) : (String, String) = {
    val apiVersion: String = (api \ "version").as[String]
    val response: HttpResponse = new Resource(s"$ramlPath/$apiVersion/application.raml", runningPort).get()
    require(response.status == 200)
    apiVersion -> response.body
  }

  private def getEndpoints(api: JsValue) = {
    (api \ "endpoints").as[List[JsValue]].map { ep =>
      val uriPattern = (ep \ "uriPattern").as[String]
      val endPointName = (ep \ "endpointName").as[String]
      Endpoint(uriPattern, endPointName, (api \ "version").as[String])
    }
  }

  def forAllApiVersions[T](generator: (JsValue) => T, versions: List[JsValue] = DefinitionsFileApiVersions)(fn: T => Unit): Unit = {
    versions.foreach(version => fn(generator(version)))
  }
}