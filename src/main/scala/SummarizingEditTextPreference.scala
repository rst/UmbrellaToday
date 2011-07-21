package org.bostonandroid.umbrellatoday;

import android.preference.EditTextPreference
import android.content.Context
import android.util.AttributeSet

class SummarizingEditTextPreference( ctx: Context, attrs: AttributeSet )
  extends EditTextPreference( ctx, attrs )
{
  override def setText( text: String ) = {
    super.setText( text )
    setSummary( text )
  }
}
