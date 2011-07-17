package org.bostonandroid.umbrellatoday

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

trait WeatherAlertScheduler 
  extends org.positronicnet.util.AppFacility
{
  private var context: Context = null

  override def realOpen( ctx: Context ) = {
    super.realOpen( ctx )
    this.context = ctx
  }

  def scheduleNextWeatherAlert = {

    val alert = WeatherAlert.findNextAlert

    val alarmManager = 
      context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

    if (true)
      Log.d( "XXX", "Not setting alerts yet" )
    else if (alert == null)
      alarmManager.cancel( makePendingIntent( context, makeIntent( context )))
    else
      alarmManager.set( AlarmManager.RTC_WAKEUP, alert.alertTime,
                        makePendingIntent(context, makeIntent(context, alert )))
  }

  private def makeIntent( c: Context ): Intent = 
    new Intent( c, classOf[ AlarmReceiver ] )

  private def makeIntent( c: Context, w: WeatherAlert ): Intent = 
    makeIntent( c ).putExtra( "alert_id", w.id )

  private def makePendingIntent( context: Context, intent: Intent ) = 
    PendingIntent.getBroadcast( context, 0, intent,
                                PendingIntent.FLAG_CANCEL_CURRENT )
}
