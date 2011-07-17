package org.bostonandroid.umbrellatoday;

import org.bostonandroid.timepreference.TimePreference

import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager

import android.util.Log

class EditAlertActivity
  extends PreferenceActivity
  with UmbrellaActivityHelpers
{
  def findPref[ T ]( s: String ):T = findPreference( s ).asInstanceOf[ T ]

  onCreate {

    addPreferencesFromResource( R.xml.alert );
    setContentView( R.layout.edit_alert );

    // Get alert to operate on.  

    val alertId = getIntent.getExtras.getLong( "alert_id", -1 )
    Log.d( "XXX", "starting EditAlertActivity, ID " + alertId )
    val alert = WeatherAlert.find( alertId )

    findPref[ CheckBoxPreference ]("enable_alert").setChecked( alert.enabled )
    findPref[ RepeatPreference ]("repeat").setChoices( alert.repeatDays )
    findPref[ CheckBoxPreference ]("detect_location").setChecked(
      alert.autolocate )
    findPref[ EditTextPreference ]("location").setText( alert.location )

    findPref[ TimePreference ]("time").setTime( 
      TimePreference.formatter.format( alert.alertTime ))
    findPref[ TimePreference ]("time").setSummary( 
      TimePreference.summaryFormatter(this).format( alert.alertTime ))

    findView( TR.update_alert ).onClick{ 

      val updatedAlert = 
        alert.alertAt( findPref[ TimePreference ]("time").getTime)
             .repeatDays( findPref[ RepeatPreference ]("repeat").getChoices)
             .autolocate( findPref[ CheckBoxPreference ]("detect_location").isChecked )
             .location( findPref[ EditTextPreference ]("location").getText )
             .enabled( findPref[ CheckBoxPreference ]("enable_alert").isChecked)

      WeatherAlert.save( updatedAlert )
    }
  }
}
