package org.bostonandroid.umbrellatoday

import org.positronicnet.ui.UiBinder

import org.bostonandroid.timepreference.TimePreference
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference

import java.util.Calendar

class WeatherUiBinder( ctx: android.content.Context )
  extends UiBinder
{
  bindProperties[ RepeatPreference, Set[String] ](
    (_.getChoices), (_.setChoices(_)))

  bindProperties[ TimePreference, Calendar ](
    (_.getTime),
    ((pref, calendar) => {
      val tyme = calendar.getTime
      pref.setTime( TimePreference.formatter.format( tyme ))
      pref.setSummary( TimePreference.summaryFormatter( ctx ).format( tyme ))
    }))
}
