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

package controllers

import connectors.{ApiDefinitionConnector, AuthConnector}
import model.APIStatus.APIStatus
import model._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Request, Result}
import services.{ApplicationService, DeveloperService}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.{GatekeeperAuthProvider, GatekeeperAuthWrapper}
import views.html.developers.developers

object DevelopersController extends DevelopersController {
  override val developerService: DeveloperService = DeveloperService
  override val applicationService: ApplicationService = ApplicationService
  override val apiDefinitionConnector: ApiDefinitionConnector = ApiDefinitionConnector
  override def authConnector = AuthConnector
  override def authProvider = GatekeeperAuthProvider
}

trait DevelopersController extends FrontendController with GatekeeperAuthWrapper {

  val applicationService: ApplicationService
  val developerService: DeveloperService
  val apiDefinitionConnector: ApiDefinitionConnector

  private def redirect(filter: Option[String], status: Option[String], pageNumber: Int, pageSize: Int) = {
    val pageParams = Map(
      "pageNumber" -> Seq(pageNumber.toString),
      "pageSize" -> Seq(pageSize.toString)
    )

    val filterParams = filter match {
      case Some("") | None => Map.empty
      case Some(flt) => Map("filter" -> Seq(flt))
    }

    val statusParams = status match {
      case Some("") | None => Map.empty
      case Some(stat) => Map("status" -> Seq(stat))
    }

    val queryParams = pageParams ++ filterParams ++ statusParams
    Redirect("", queryParams, 303)
  }


  private def groupApisByStatus(apis: Seq[APIDefinition]): Map[APIStatus, Seq[VersionSummary]] = {
    
    val versions = for {
      api <- apis
      version <- api.versions
    } yield VersionSummary(api.name, version.status, APIIdentifier(api.context, version.version))

    versions.groupBy(_.status)
  }

  protected def validPageResult(page: PageableCollection[User], emails: String, apis: Seq[APIDefinition], filter: Option[String], status: Option[String])(implicit request: Request[_]): Result =
    Ok(developers(page, emails, groupApisByStatus(apis), filter, status))
  
  def developersPage(filter: Option[String], status: Option[String], optionalPageNumber: Option[Int], optionalPageSize: Option[Int]) = requiresRole(Role.APIGatekeeper) {
    implicit request => implicit hc =>
      
      val apiFilter = ApiFilter(filter)
      val statusFilter = StatusFilter(status)

      val pageSize = optionalPageSize.getOrElse(100)
      val pageNumber = optionalPageNumber.getOrElse(1)
      
      for {
        apps <- applicationService.fetchApplications(apiFilter)
        apis <- apiDefinitionConnector.fetchAll
        filterOps = (developerService.filterUsersBy(apiFilter, apps) _
                     andThen developerService.filterUsersBy(statusFilter))
        allUsers <- developerService.fetchDevelopers
        filteredUsers = filterOps(allUsers)
        emails = Developers(filteredUsers).emailList
        page = PageableCollection(filteredUsers, pageNumber, pageSize)
      } yield {
        if (page.valid) {
          validPageResult(page, emails, apis, filter, status)
        }
        else {
          redirect(filter, status, 1, pageSize)
        }
      }
  }
}
