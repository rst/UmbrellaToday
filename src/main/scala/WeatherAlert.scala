package org.bostonandroid.umbrellatoday

import org.positronicnet.db._

import org.positronicnet.util.WorkerThread
import org.positronicnet.util.ChangeManager

import android.content.ContentValues
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

object WeatherAlert 
  extends ChangeManager( WeatherAlertDb )
{
  // Database operations

  private val UNSAVED_ID = -1
  private val table = WeatherAlertDb( "alerts" )

  def find( id: Long ) = WeatherAlert.fromDbRows( table.whereEq("_id" -> id))(0)

  def findNextAlert: WeatherAlert = {

    val enabledAlerts = table.whereEq( "enabled" -> true )
    var bestYet: WeatherAlert = null
    var bestTime: Long = -1

    for (nextAlert <- WeatherAlert.fromDbRows( enabledAlerts )) {
      if ( bestYet == null || nextAlert.alertTime < bestYet.alertTime )
        bestYet = nextAlert
    }

    return bestYet
  }

  def save( w: WeatherAlert ) = doChange {
    Log.d( "XXX", "Entering save with id " + w.id.toString )
    if ( w.id == WeatherAlert.UNSAVED_ID ) {
      Log.d( "XXX", "Insert" )
      table.insert( updateValues( w ): _* )
    }
    else {
      Log.d( "XXX", "Update" )
      table.whereEq( "_id" -> w.id ).update( updateValues( w ): _* )
    }
  }

  def disable( w: WeatherAlert ) = save( w.enabled( false ))

  def deleteWithId( id: Long ) = doChange {
    table.whereEq( "_id" -> id ).delete
  }

  // A "source" for the current list of WeatherAlerts, which will automatically
  // requery whenever we "doChange" above...

  lazy val all = valueStream{ WeatherAlert.fromDbRows( table.order("_id asc"))}

  // and number available...

  lazy val count = valueStream{ table.count }

  // Also force rescheduling on any change

  val force_resched = valueStream{ WeatherAlertDb.scheduleNextWeatherAlert }

  // Database donkey work

  private def fromDbRows( query: DbQuery ) = 
    for (c <- query.select( "_id", "alert_at",
                            "sunday", "monday", "tuesday", "wednesday", 
                            "thursday", "friday", "saturday",
                            "location", "autolocate", "enabled" ))
    yield WeatherAlert( c.getLong( 0 ),                      // id
                        calendarize( c.getString ( 1 )),     // alert_at
                        c.getBoolean( 2 ),                   // sunday
                        c.getBoolean( 3 ),                   // monday
                        c.getBoolean( 4 ),                   // tuesday
                        c.getBoolean( 5 ),                   // wednesday
                        c.getBoolean( 6 ),                   // thursday
                        c.getBoolean( 7 ),                   // friday
                        c.getBoolean( 8 ),                   // saturday
                        c.getString( 9 ),                    // location
                        c.getBoolean( 10 ),                  // autolocate
                        c.getBoolean( 11 ))                  // enabled

  private def updateValues( w: WeatherAlert ): Seq[( String, SqlValue )] = {
    Seq( "alert_at"   -> stringize( w.alertAt ),
         "sunday"     -> w.sunday,
         "monday"     -> w.monday,
         "tuesday"    -> w.tuesday,
         "wednesday"  -> w.wednesday,
         "thursday"   -> w.thursday,
         "friday"     -> w.friday,
         "saturday"   -> w.saturday,
         "location"   -> w.location,
         "autolocate" -> w.autolocate,
         "enabled"    -> w.enabled )
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

  private val weekdays = Array( "Sunday", "Monday", "Tuesday", "Wednesday",
                                "Thursday", "Friday", "Saturday" )
}

case class WeatherAlert( id:         Long     = WeatherAlert.UNSAVED_ID,
                         alertAt:    Calendar = new GregorianCalendar(1970,1,1),
                         sunday:     Boolean  = false,
                         monday:     Boolean  = false,
                         tuesday:    Boolean  = false,
                         wednesday:  Boolean  = false,
                         thursday:   Boolean  = false,
                         friday:     Boolean  = false,
                         saturday:   Boolean  = false,
                         location:   String   = null,
                         autolocate: Boolean  = false,
                         enabled:    Boolean  = false )
{
  // Convenience pseudo-columns

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

  def dinkedCopy( id:         Long     = this.id,       
                  alertAt:    Calendar = this.alertAt,
                  sunday:     Boolean  = this.sunday,   
                  monday:     Boolean  = this.monday, 
                  tuesday:    Boolean  = this.tuesday,  
                  wednesday:  Boolean  = this.wednesday,
                  thursday:   Boolean  = this.thursday, 
                  friday:     Boolean  = this.friday,
                  saturday:   Boolean  = this.saturday,
                  location:   String   = this.location, 
                  autolocate: Boolean  = this.autolocate,
                  enabled:    Boolean  = this.enabled ) =
    WeatherAlert( id, alertAt, 
                  sunday, monday, tuesday, wednesday, 
                  thursday, friday, saturday,
                  location, autolocate, enabled )

  def alertAt    ( t: Calendar ) = dinkedCopy( alertAt    = t )
  def location   ( l: String   ) = dinkedCopy( location   = l )
  def autolocate ( b: Boolean  ) = dinkedCopy( autolocate = b )
  def enabled    ( b: Boolean  ) = dinkedCopy( enabled    = b )
  
  def repeatDays ( days: Set[ String ] ) =
    dinkedCopy( sunday    = days.contains( "Sunday" ),
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

    if (alertTime.after( now ))         // Missed already.  Same time tomorrow?
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
    return WeatherAlert.weekdays( cal_day_idx )
  }

}
                         
