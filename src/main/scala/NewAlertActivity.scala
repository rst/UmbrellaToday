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
  val binder = new WeatherUiBinder( this )

  onCreate {

    addPreferencesFromResource( R.xml.alert );
    setContentView( R.layout.new_alert );

    // Force initial time choice...
    getPreferenceScreen().onItemClick(null, null, 1, 0);

    findView( TR.save_alert ).onClick{ 
      val newAlert = binder.update( new WeatherAlert, getPreferenceScreen )
      WeatherAlerts ! Save( newAlert )
      toastAlert( newAlert )
      setResult( Activity.RESULT_OK )
      finish()
    }
  }
}
