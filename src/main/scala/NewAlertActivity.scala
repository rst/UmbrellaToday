package org.bostonandroid.umbrellatoday;

import org.bostonandroid.timepreference.TimePreference

import org.positronicnet.orm._
import org.positronicnet.orm.Actions._

import android.app.Activity

import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager

class NewAlertActivity
  extends PreferenceActivity
  with UmbrellaActivityHelpers
{
  def findPref[ T ]( s: String ):T = findPreference( s ).asInstanceOf[ T ]

  onCreate {

    addPreferencesFromResource( R.xml.alert );
    setContentView( R.layout.new_alert );

    val alert = new WeatherAlert

    // Force initial time choice...
    getPreferenceScreen().onItemClick(null, null, 1, 0);

    findView( TR.save_alert ).onClick{ 

      val newAlert = 
        (new WeatherAlert)
          .alertAt( findPref[ TimePreference ]("alertAt").getTime)
          .repeatDays( findPref[ RepeatPreference ]("repeatDays").getChoices)
          .autolocate( findPref[ CheckBoxPreference ]("autolocate").isChecked )
          .location( findPref[ EditTextPreference ]("location").getText )
          .enabled( findPref[ CheckBoxPreference ]("enabled").isChecked)

      WeatherAlerts ! Save( newAlert )
      toastAlert( newAlert )
      setResult( Activity.RESULT_OK )
      finish()
    }
  }
}
