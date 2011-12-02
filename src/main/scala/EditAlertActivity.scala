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
  val binder = new WeatherUiBinder( this )

  onCreate {

    addPreferencesFromResource( R.xml.alert );
    setContentView( R.layout.edit_alert );

    val alertId = getIntent.getExtras.getLong( "alert_id", -1 )

    WeatherAlerts ! Find( alertId ){ alert => {

      binder.show( alert, getPreferenceScreen )

      findView( TR.update_alert ).onClick{ 
        val updatedAlert = binder.update( alert, getPreferenceScreen )
        WeatherAlerts ! Save( updatedAlert )
        toastAlert( updatedAlert )
      }
    }}
  }
}
