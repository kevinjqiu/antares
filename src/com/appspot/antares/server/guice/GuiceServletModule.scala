package com.appspot.antares.server.guice

import com.google.inject.servlet.ServletModule
import com.appspot.antares.server.servlets.AntaresServlet

class GuiceServletModule extends ServletModule {

  override def configureServlets() {
    serve("/m").`with`(classOf[AntaresServlet])
  }

}
