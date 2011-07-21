package org.bostonandroid.umbrellatoday;

import org.bostonandroid.timepreference.TimePreference

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
             .alertAt( findPref[ TimePreference ]("time").getTime)
             .repeatDays( findPref[ RepeatPreference ]("repeat").getChoices)
             .autolocate( findPref[ CheckBoxPreference ]("detect_location").isChecked )
             .location( findPref[ EditTextPreference ]("location").getText )
             .enabled( findPref[ CheckBoxPreference ]("enable_alert").isChecked)

      WeatherAlert.save( newAlert )
      toastAlert( newAlert )
      setResult( Activity.RESULT_OK )
      finish()
    }
  }
}
