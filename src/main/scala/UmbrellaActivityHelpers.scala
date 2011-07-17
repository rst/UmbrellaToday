package org.bostonandroid.umbrellatoday

import org.positronicnet.ui._

trait UmbrellaActivityHelpers
  extends PositronicActivityHelpers
{
  def findView[T](  tr: TypedResource[T] ) = 
    findViewById( tr.id ).asInstanceOf[T]

  def findViewById( id: Int ): android.view.View
}
