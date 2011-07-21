package org.bostonandroid.umbrellatoday;

import android.preference.ListPreference

import android.app.AlertDialog

import android.content.DialogInterface
import android.content.Context

import android.util.AttributeSet

class RepeatPreference( ctx: Context, attrs: AttributeSet )
  extends ListPreference( ctx, attrs )
{
  private var currentChoices: Set[ String ] = Set()
  private var newChoices: Set[ String ] = Set()

  def setChoices( choices: Set[ String ] ): Unit = {
    this.currentChoices = choices
    this.newChoices     = choices
    resetSummary
  }

  def getChoices = currentChoices

  override def onDialogClosed( positiveResult: Boolean ): Unit = {
    if (positiveResult) {
      this.currentChoices = this.newChoices
      resetSummary
    }
  }

  override protected def onPrepareDialogBuilder( builder: AlertDialog.Builder)={

    val entryVals = getEntryValues

    val listener = new DialogInterface.OnMultiChoiceClickListener {
      def onClick( dialog: DialogInterface, which: Int, isChecked: Boolean ) =
        if (isChecked) 
          newChoices = newChoices + entryVals( which ).toString
        else
          newChoices = newChoices - entryVals( which ).toString
    }

    val entrySelected = 
      for ( value <- entryVals ) 
      yield currentChoices.contains( value.toString )

    builder.setMultiChoiceItems( entryVals, entrySelected, listener )
  }

  protected def resetSummary = {
    setSummary( if (currentChoices.isEmpty) "Never"
                else currentChoices.toArray.reduceLeft{ _ + "," + _ } )
  }
}
