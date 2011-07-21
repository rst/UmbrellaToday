package org.bostonandroid.umbrellatoday

import org.positronicnet.ui._
import android.text.format.DateFormat

trait UmbrellaActivityHelpers
  extends PositronicActivityHelpers
{
  def findView[T](  tr: TypedResource[T] ) = 
    findViewById( tr.id ).asInstanceOf[T]

  def findViewById( id: Int ): android.view.View

  def toastAlert( alert: WeatherAlert ) = {
    if (alert.enabled) {
      val dateFmt = DateFormat.getDateFormat( this )
      val timeFmt = DateFormat.getTimeFormat( this )
      toastLong( "Alert set for " + dateFmt.format( alert.alertTime ) +
                 " at " + timeFmt.format( alert.alertTime ))
    }
  }
}
