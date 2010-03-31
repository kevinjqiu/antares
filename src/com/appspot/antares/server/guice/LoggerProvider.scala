package com.appspot.antares.server.guice

import com.google.inject.Provider
import java.util.logging.Logger

class LoggerProvider extends Provider[Logger] {

  def get() = {
    Logger.getLogger("com.appspot.antares")
  }

}
