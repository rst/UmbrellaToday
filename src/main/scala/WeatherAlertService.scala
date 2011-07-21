package org.bostonandroid.umbrellatoday;

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver;

import android.location.LocationManager

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.util.Log

import com.commonsware.cwac.wakeful.WakefulIntentService

class WeatherAlertService 
  extends WakefulIntentService( "WeatherAlertService" )
  with UmbrellaTodayClient
{
  override protected def doWakefulWork( intent: Intent ): Unit = {

    val alertId  = intent.getExtras.getLong( "alert_id" )
    val alertRec = WeatherAlert.find( alertId )
    val location = (if (alertRec.autolocate) maybeCurrentLocation
                    else alertRec.location)
    val url = (if (location == null) null
               else retrieveLocationUrl( location ))

    if (url == null)
      showErrorNotification( "Can't find location for weather check", alertId )
    else {
      val needUmbrella = getForecast( url )

      if ( needUmbrella == "snow" )
        showNotification( R.drawable.weather_snow, "Bring a shovel!", alertId )
      else if ( needUmbrella == "yes" )
        showNotification( R.drawable.weather_showers,
                          "You may need your galoshes!",
                          alertId )
      else if ( needUmbrella == null )
        showErrorNotification("Garbled forecast for location", alertId)
    }

    if (! alertRec.isRepeating ) {
      // Disable this alert.  Forces a reschedule, like any other change.
      WeatherAlert.disable( alertRec )
    }
    else {
      // No changes in DB.  Reschedule anyway.
      WeatherAlertDb.scheduleNextWeatherAlert
    }
  }

  // Notifications

  lazy val notificationManager =
    getSystemService(Context.NOTIFICATION_SERVICE)
      .asInstanceOf[ NotificationManager ]

  def buildNotification( icon: Int, text: String, alertId: Long ):Notification={

    val notification = new Notification( icon, text, System.currentTimeMillis )

    val editIntent = 
      new Intent( this, 
                  classOf[ EditAlertActivity ]).putExtra( "alert_id", alertId )

    val action = PendingIntent.getActivity( this, 0, editIntent, 0 )

    notification.setLatestEventInfo( this, 
                                     getString( R.string.app_name ), 
                                     text, action )

    return notification
  }
  
  def showNotification( icon: Int, text: String, alertId: Long ) = {
    val notificationId = alertId.asInstanceOf[ Int ]
    notificationManager.cancel( notificationId )
    notificationManager.notify( notificationId,
                                buildNotification( icon, text, alertId ))
  }

  def showErrorNotification( text: String, alertId: Long ) = 
    showNotification( R.drawable.weather_showers, text, alertId )

  // Location handling

  lazy val locationManager = 
    getSystemService(Context.LOCATION_SERVICE).asInstanceOf[ LocationManager ]

  private val locationProvider = LocationManager.NETWORK_PROVIDER

  def maybeCurrentLocation =
    if ( !locationManager.isProviderEnabled( locationProvider ))
      null
    else {
      val loc = locationManager.getLastKnownLocation( locationProvider )
      loc.getLatitude + "," + loc.getLongitude
    }
}

// Glue code to wake up the service when we get an alarm

class AlarmReceiver 
  extends BroadcastReceiver 
{
  override def onReceive( context: Context, intent: Intent ): Unit = {
    val serviceIntent = new Intent(context, classOf[ WeatherAlertService ])
    serviceIntent.putExtras( intent.getExtras )
    WakefulIntentService.sendWakefulWork( context, serviceIntent );
  }
}

// Glue code to reset the alarm on system boot or power-up

class ResetAlarmReceiver 
  extends BroadcastReceiver 
{
  override def onReceive(c: Context, i: Intent): Unit = {
    Log.d( UmbrellaTodayApplication.logTag, "Setting alarm" );
    WeatherAlertDb.scheduleNextWeatherAlert
  }
}
