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

package controllers

import controllers.actions.IdentifierAction
import jakarta.inject.Inject
import models.{Mode, UploadedFile}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.Files.TemporaryFile
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{FileUploaded, FileUploaderView}

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.ExecutionContext

class FileUploadController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    fileUploaderView: FileUploaderView,
    fileUploadedView: FileUploaded
)(using ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = identify { implicit request =>
    logger.info(s"File Uploading Page")
    Ok(fileUploaderView())
  }

  def upload(mode: Mode): Action[MultipartFormData[TemporaryFile]] = {
    identify(parse.multipartFormData) { implicit request =>
      request.body.file("fileUpload1") match {
        case Some(filePart) =>
          val uploadDir: Path = getUploadLocation()
          // Use the original filename, but strip any path information
          val safeFilename =
            Paths.get(filePart.filename).getFileName.toString
          val destination: Path =
            uploadDir.resolve(safeFilename)
          // Move the temporary file to the destination
          filePart.ref.moveTo(destination.toFile)
          // Gather metadata AFTER the move
          val storedFile = destination.toFile
          val result     = UploadedFile(
            filename = safeFilename,
            contentType = filePart.contentType,
            size = storedFile.length(),
            location = destination.toAbsolutePath
          )
          logger.info(
            s"File uploaded: ${result.filename}, ${result.size} bytes"
          )
          Ok(fileUploadedView(List(result)))
        case None =>
          logger.warn("No file uploaded")
          BadRequest(fileUploaderView())
      }
    }
  }

  def uploadFiles(mode: Mode): Action[MultipartFormData[TemporaryFile]] = {
    identify(parse.multipartFormData) { implicit request =>
      if (request.body.files.isEmpty) {
        BadRequest(fileUploaderView())
      } else {
        val uploadDir     = getUploadLocation()
        val uploadedFiles = request.body.files.map { filePart =>
          val destination = uploadDir.resolve(filePart.filename)
          filePart.ref.moveTo(destination, true)
          UploadedFile(
            filename = filePart.filename,
            contentType = filePart.contentType,
            size = Files.size(destination),
            location = destination
          )
        }
        Ok(fileUploadedView(uploadedFiles))
      }
    }
  }

  private def getUploadLocation(): Path = {
    val uploadDir: Path =
      Paths.get("uploads")
    Files.createDirectories(uploadDir)
  }
}
