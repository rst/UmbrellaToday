package org.bostonandroid.umbrellatoday;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.UnsupportedEncodingException;
import java.io.IOException

import android.util.Log

import scala.xml.XML

trait UmbrellaTodayClient {

  // The UmbrellaToday HTTP service works like this:
  //
  // If you have a location, and you feed it in with a POST to
  // forecastsUrl, you'll get back a 201 Created response, with
  // an URL that you want to GET to get the actual forecast.  
  // Or, nothing at all if the location you provided couldn't 
  // be resolved.
  //
  // You then need to append '.xml', if that's what you want, and
  // GET the forecast.
  //
  // We want to distinguish "couldn't parse location" from "couldn't
  // get forecast" for the user.  So, we've got routines here for both steps.

  val forecastsUrl = "http://umbrellatoday.com/forecasts"

  // First, location lookup.  Returns the location to GET for
  // your forecast, or null.

  def retrieveLocationUrl( location: String ): String = {

    val req = httpXmlPostRequest( forecastsUrl, locationEntity( location ))
    val resp = doHttpPost( req )

    if (resp == null) return null
    if (resp.getStatusLine.getStatusCode != HttpStatus.SC_CREATED) return null

    val hdr = resp.getFirstHeader("Location")
    if (hdr == null) return null;

    return hdr.getValue + ".xml"
  }

  private def locationEntity( location: String ):StringEntity = {
    val xml = <forecast><location-name>{location}</location-name></forecast>
    new StringEntity( xml.toString )
  }

  private def doHttpPost( req: HttpPost ): HttpResponse = {
    try {
      return (new DefaultHttpClient).execute( req )
    }
    catch {
      case _: IOException =>
        req.abort
        return null
    }
  }

  private def httpXmlPostRequest( url: String, body: StringEntity ):HttpPost = {
    val postRequest = new HttpPost(url);
    postRequest.addHeader("Accept", "text/xml");
    postRequest.addHeader("Content-Type", "text/xml");
    postRequest.addHeader("User-Agent", "Android Umbrella Today/1.0");
    postRequest.setEntity( body );
    return postRequest;
  }

  // Now, retrieving a forecast from one of the URLs fetched above.
  // Here, library routines do most of the heavy lifting...

  def getForecast( url: String ):String = {

    try {
      val response = XML.load( new java.net.URL( url ))
      val answer = response \ "answer"
      if (answer == scala.xml.NodeSeq.Empty)
        return null
      else
        return answer.text
    }
    catch { case e: Exception =>

      // Something blew up.  The only people who care about the details
      // are reading "adb logcat".  So...

      Log.e( UmbrellaTodayApplication.logTag, "error retrieving forecast", e)
      return null
    }
  }
}

