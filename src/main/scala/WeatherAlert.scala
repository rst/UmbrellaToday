package org.bostonandroid.umbrellatoday

import org.positronicnet.db._
import org.positronicnet.orm._
import org.positronicnet.notifications.Actions._

import android.util.Log

import java.util.Calendar
import java.util.GregorianCalendar

import java.text.SimpleDateFormat;
import java.text.ParseException;

import scala.collection.mutable.HashSet

object WeatherAlertDb 
  extends Database( filename = "UmbrellaToday", 
                    logTag   = UmbrellaTodayApplication.logTag ) 
  with WeatherAlertScheduler
{
  // Compatible with prior schema.  FWIW, this includes BOOLEAN and TIME
  // types which Sqlite doesn't formally support.  It fakes them up by
  // treating BOOLEAN as INTEGER (for which the standard Positronic 
  // conversions suffice), and TIME as STRING, which is an "HH:MM"
  // string, internally represented as a Calendar.
  //
  // (I might want to add a special-case column conversion facility
  // to cover that last bit, but it's kind of dubious for general use...)

  def schemaUpdates =
    List("""CREATE TABLE alerts (_id INTEGER PRIMARY KEY, 
                                 alert_at TIME, 
                                 sunday BOOLEAN, 
                                 monday BOOLEAN, 
                                 tuesday BOOLEAN, 
                                 wednesday BOOLEAN, 
                                 thursday BOOLEAN, 
                                 friday BOOLEAN, 
                                 saturday BOOLEAN, 
                                 location VARCHAR(255), 
                                 autolocate BOOLEAN, 
                                 enabled BOOLEAN)""")
  
  // Seems I need a "RecreateOnUpgrade" trait.  In the meantime...

  override def version = 4
}

object WeatherAlerts
  extends RecordManager[ WeatherAlert ]( WeatherAlertDb( "alerts" ))
{
  // Our WeatherAlert records present "alertAt" as a Calendar,
  // converting as needed from the underlying string, which they
  // call "rawAlertAt".  It's the string that gets persisted into
  // the "alert_at" column; that's special-cased here:

  mapField( "rawAlertAt", "alert_at" )

  // Force rescheduling on any change

  this ! AddWatcher( this ){ dummy => WeatherAlertDb.scheduleNextWeatherAlert }

  // Next alert we'll need to schedule.  (We only do one at a time.)

  def findNextAlert: WeatherAlert = {

    val enabledAlerts = this.whereEq( "enabled" -> true ).fetchOnThisThread
    var bestYet: WeatherAlert = null
    var bestTime: Long = -1

    for (nextAlert <- enabledAlerts ) {
      if ( bestYet == null || nextAlert.alertTime < bestYet.alertTime )
        bestYet = nextAlert
    }

    return bestYet
  }
}

case class WeatherAlert( rawAlertAt: String   = "00:00",
                         sunday:     Boolean  = false,
                         monday:     Boolean  = false,
                         tuesday:    Boolean  = false,
                         wednesday:  Boolean  = false,
                         thursday:   Boolean  = false,
                         friday:     Boolean  = false,
                         saturday:   Boolean  = false,
                         location:   String   = null,
                         autolocate: Boolean  = false,
                         enabled:    Boolean  = false,
                         id:         Long     = ManagedRecord.unsavedId
                       )
  extends ManagedRecord( WeatherAlerts )
  with org.positronicnet.util.ReflectiveProperties // XXX
{
  // Convenience pseudo-columns

  lazy val alertAt = calendarize( rawAlertAt )

  lazy val alertTime = computeNextAlertTimeInMillis
  lazy val isRepeating = sunday || monday || tuesday || wednesday ||
                         thursday || friday || saturday

  lazy val repeatDays: Set[ String ] = {
    val b = new HashSet[ String ]
    if (sunday)    b += "Sunday"
    if (monday)    b += "Monday"
    if (tuesday)   b += "Tuesday"
    if (wednesday) b += "Wednesday"
    if (thursday)  b += "Thursday"
    if (friday)    b += "Friday"
    if (saturday)  b += "Saturday"
    b.toSet
  }

  // "Fluid updates"

  def location_:=   ( l: String   ): WeatherAlert = copy( location   = l )
  def autolocate_:= ( b: Boolean  ): WeatherAlert = copy( autolocate = b )
  def enabled_:=    ( b: Boolean  ): WeatherAlert = copy( enabled    = b )
  def alertAt_:=    ( t: Calendar ): WeatherAlert = 
    copy( rawAlertAt = stringize( t ))
  
  def repeatDays_:= ( days: Set[ String ] ) =
    copy( sunday    = days.contains( "Sunday" ),
          monday    = days.contains( "Monday" ),
          tuesday   = days.contains( "Tuesday" ),
          wednesday = days.contains( "Wednesday" ),
          thursday  = days.contains( "Thursday" ),
          friday    = days.contains( "Friday" ),
          saturday  = days.contains( "Saturday" ))

  // Calendar mummery

  private def computeNextAlertTimeInMillis: Long = {
    
    val now = new GregorianCalendar
    val alertTime = now.clone.asInstanceOf[ Calendar ]

    alertTime.set( Calendar.HOUR_OF_DAY, alertAt.get( Calendar.HOUR_OF_DAY ))
    alertTime.set( Calendar.MINUTE,      alertAt.get( Calendar.MINUTE ))
    alertTime.clear( Calendar.SECOND )
    alertTime.clear( Calendar.MILLISECOND )

    if (now.after( alertTime ))         // Missed already.  Same time tomorrow?
      bumpOneDay( alertTime )

    if ( isRepeating ) {
      while (!repeatsFor( alertTime ))
        bumpOneDay( alertTime )
    }

    return alertTime.getTimeInMillis
  }

  private def bumpOneDay( c: Calendar ) = c.add( Calendar.DAY_OF_WEEK, 1 )
  private def repeatsFor( c: Calendar ) = repeatDays.contains( dayOfWeek( c ))

  private def dayOfWeek( c: Calendar ):String = {

    // This code assumes that in Gregorian calendars, Sunday is explicitly
    // the first day of the week, and not, say, the last...

    val cal_day_idx = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val weekdays = Array( "Sunday", "Monday", "Tuesday", "Wednesday",
                          "Thursday", "Friday", "Saturday" )
    return weekdays( cal_day_idx )
  }

  // Conversions

  lazy val dateFormatter = new SimpleDateFormat( "kk:mm" )

  private def stringize( c: Calendar ) = dateFormatter.format( c.getTime )

  private def calendarize( s: String ): Calendar = {
    try {
      val c = Calendar.getInstance
      c.setTime( dateFormatter.parse( s ))
      return c
    }
    catch { 
      case e: ParseException =>
        Log.e( UmbrellaTodayApplication.logTag, "time parse error", e );
        return new GregorianCalendar(1970,01,01);
    }
  }
}
                         
