package com.appspot.antares.server.guice

import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Guice


class GuiceServletConfig extends GuiceServletContextListener {

  protected def getInjector() = {
    Guice.createInjector(new GuiceServerModule(), new GuiceServletModule())
  }

}