package net.rouly.employability.web

import net.rouly.common.server.play.module.{AppServerComponents, AppServerLoader}
import play.api.{ApplicationLoader, BuiltInComponentsFromContext}

class AppLoader extends AppServerLoader {

  override def buildComponents(context: ApplicationLoader.Context): AppServerComponents =
    new BuiltInComponentsFromContext(context) with AppComponents

}
