package org.bostonandroid.umbrellatoday

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.text.format.DateFormat

trait WeatherAlertScheduler 
  extends org.positronicnet.facility.AppFacility
{
  private var context: Context = null

  override def realOpen( ctx: Context ) = {
    super.realOpen( ctx )
    this.context = ctx
  }

  def scheduleNextWeatherAlert = {

    val alert = WeatherAlerts.findNextAlert

    val alarmManager = 
      context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]

    if (alert == null) {
      Log.d( UmbrellaTodayApplication.logTag, "Canceling weather alerts")
      alarmManager.cancel( makePendingIntent( context, makeIntent( context )))
    }
    else {
      val dateFmt = DateFormat.getDateFormat( context )
      val timeFmt = DateFormat.getTimeFormat( context )
      Log.d( UmbrellaTodayApplication.logTag, 
             "Scheduling next UmbrellaToday alarm for " + 
             dateFmt.format( alert.alertTime ) +
             " at " + timeFmt.format( alert.alertTime ))
      alarmManager.set( AlarmManager.RTC_WAKEUP, alert.alertTime,
                        makePendingIntent(context, makeIntent(context, alert )))
    }
  }

  private def makeIntent( c: Context ): Intent = 
    new Intent( c, classOf[ AlarmReceiver ] )

  private def makeIntent( c: Context, w: WeatherAlert ): Intent = 
    makeIntent( c ).putExtra( "alert_id", w.id.id )

  private def makePendingIntent( context: Context, intent: Intent ) = 
    PendingIntent.getBroadcast( context, 0, intent,
                                PendingIntent.FLAG_CANCEL_CURRENT )
}
