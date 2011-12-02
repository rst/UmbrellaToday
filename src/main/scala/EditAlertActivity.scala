package org.bostonandroid.umbrellatoday;

import org.positronicnet.orm._
import org.positronicnet.orm.Actions._

import org.bostonandroid.timepreference.TimePreference

import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager

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

    WeatherAlerts ! Find( alertId ){ alert => {

      findPref[ CheckBoxPreference ]("enabled").setChecked( alert.enabled )
      findPref[ RepeatPreference ]("repeatDays").setChoices( alert.repeatDays )
      findPref[ CheckBoxPreference ]("autolocate").setChecked(
        alert.autolocate )
      findPref[ EditTextPreference ]("location").setText( alert.location )

      findPref[ TimePreference ]("alertAt").setTime( 
        TimePreference.formatter.format( alert.alertTime ))
      findPref[ TimePreference ]("alertAt").setSummary( 
        TimePreference.summaryFormatter(this).format( alert.alertTime ))

      findView( TR.update_alert ).onClick{ 

        val updatedAlert = 
          alert.alertAt( findPref[ TimePreference ]("alertAt").getTime)
           .repeatDays( findPref[ RepeatPreference ]("repeatDays").getChoices)
           .autolocate( findPref[ CheckBoxPreference ]("autolocate").isChecked )
           .location( findPref[ EditTextPreference ]("location").getText )
           .enabled( findPref[ CheckBoxPreference ]("enabled").isChecked)

        WeatherAlerts ! Save( updatedAlert )
        toastAlert( updatedAlert )
      }
    }}
  }
}
