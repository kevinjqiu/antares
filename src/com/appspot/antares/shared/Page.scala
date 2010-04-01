package com.appspot.antares.shared

import javax.jdo.annotations._
import java.util.Date

@PersistenceCapable(identityType=IdentityType.APPLICATION)
class Page {

  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
  private var id:String

  @Persistent
  private var lastUpdated:Date
  
  
}