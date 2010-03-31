package com.appspot.antares.server.guice

import com.google.inject.{AbstractModule, Singleton}
import java.util.logging.Logger

class GuiceServerModule extends AbstractModule {

  override def configure() {
    bind(classOf[Logger]).toProvider(classOf[LoggerProvider]).in(classOf[Singleton])
  }
}