package org.bostonandroid.umbrellatoday

import org.positronicnet.ui._
import android.content.Intent;
import android.webkit.WebView;

class WelcomeActivity
  extends PositronicActivity( layoutResourceId = R.layout.welcome )
  with UmbrellaActivityHelpers
{
  onCreate {
    findView( TR.add_alert_welcome ).onClick {
      startActivityForResult( new Intent(this, classOf[ NewAlertActivity ]), 1)
    }
  }

  override def onActivityResult( reqCode: Int, result: Int, data: Intent ) = {
    // We only make one request, so this must be the result...
    startActivity( new Intent( this, classOf[ AlertsActivity ]))
  }
}

class AboutActivity
  extends PositronicActivity( layoutResourceId = R.layout.about )
  with UmbrellaActivityHelpers
{
  onCreate {
    val webView = findViewById( R.id.about ).asInstanceOf[ WebView ]
    webView.loadUrl("file:///android_asset/about.html")
  }
}

