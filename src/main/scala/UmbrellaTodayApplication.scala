package org.bostonandroid.umbrellatoday

import android.app.Application;
import android.content.Context;

class UmbrellaTodayApplication
  extends Application
{
  override def onCreate = WeatherAlertDb.openInContext( this )
  override def onTerminate = WeatherAlertDb.close
}

object UmbrellaTodayApplication {
  val logTag = "UmbrellaToday"
}

