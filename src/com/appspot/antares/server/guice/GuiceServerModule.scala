package com.appspot.antares.server.guice

import java.util.logging.Logger
import com.google.inject.{Provides, AbstractModule, Singleton}

class GuiceServerModule extends AbstractModule {

  override def configure() {
//    bind(classOf[Logger]).toProvider(classOf[LoggerProvider]).in(classOf[Singleton])
  }
}