package org.bostonandroid.umbrellatoday

import org.positronicnet.ui.PositronicActivityHelpers
import org.positronicnet.ui.IndexedSeqSourceAdapter

import org.positronicnet.orm._
import org.positronicnet.orm.Actions._

import android.app.ListActivity
import android.content.Intent
import android.text.format.DateFormat
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

import android.util.Log

class WeatherAlertsAdapter( activity: AlertsActivity )
  extends IndexedSeqSourceAdapter( 
    activity,
    WeatherAlerts.records,
    itemViewResourceId = android.R.layout.simple_list_item_checked )
{
  override def bindView( view: View, alert: WeatherAlert ) = {
    val ctv = view.asInstanceOf[ android.widget.CheckedTextView ]
    ctv.setText( DateFormat.getTimeFormat( activity ).format( alert.alertTime ))
    ctv.setChecked( alert.enabled )
  }

  override def getItemId( posn: Int ) = seq( posn ).id
}

class AlertsActivity
  extends ListActivity
  with UmbrellaActivityHelpers
{
  onCreate {

    // If we ever have no alerts, replace ourselves immediately with
    // the WelcomeActivity.  And if the number changes (e.g., due to
    // a delete), reschedule

    onChangeTo( WeatherAlerts.count ){ count =>
      if (count == 0) {
        startActivity( new Intent( this, classOf[ WelcomeActivity ]));
        finish
      }
    }

    // Wire things up...
    // Note that since this is a ListActivity (not changing that yet),
    // clicks on the list items themselves are handled by onListItemClick below.

    useOptionsMenuResource( R.menu.main_menu )
    useContextMenuResource( R.menu.context_menu )

    this.setContentView( R.layout.alerts )
    this.setListAdapter( new WeatherAlertsAdapter( this ))

    findView( TR.add_alert ).onClick {
      startActivity( new Intent( this, classOf[ NewAlertActivity ]))
    }

    registerForContextMenu( getListView() )

    onContextItemSelected( R.id.edit ){ 
      ( menuInfo, view ) => editAlert( getItemId( menuInfo ))
    }
    onContextItemSelected( R.id.delete ){ 
      ( menuInfo, view ) => 
        WeatherAlerts.whereEq( "_id" -> getItemId( menuInfo )) ! DeleteAll
    }
    onOptionsItemSelected( R.id.about_button ) {
      startActivity( new Intent( this, classOf[ AboutActivity ]))
    }
  }

  override def onListItemClick( l: ListView, v: View, posn: Int, id: Long ) = {
    super.onListItemClick( l, v, posn, id )
    editAlert( id )
  }

  def editAlert( id: Long ) = {
    val intent = new Intent( this, classOf[ EditAlertActivity ])
    intent.putExtra( "alert_id", id )
    startActivity( intent )
  }

  // Don't have the "positronic listview" helpers in a ListActivity, so...

  def getItemId( info: ContextMenu.ContextMenuInfo ) = 
    info.asInstanceOf[ AdapterView.AdapterContextMenuInfo ].id
}

