Umbrella Today, the Android App, shanghaied into service as a Scala demo
------------------------------------------------------------------------

A rewrite of an app to tell you whether you need your umbrella, today.

This version is written in Scala, using the Positronic Net library
(version 0.1).  The build procedure, using sbt (the "simple build
tool" --- or so they call it) are something like the following:

First, install the Android SDK, sbt, and the Positronic Net library
itself following instructions [here](http://rst.github.com/tut_sections/2001/01/01/installation.html).

Then compile and build this app:

    $ cd [your workspace]
    $ git clone https://github.com/rst/UmbrellaToday.git
    $ cd UmbrellaToday
    $ sbt android:package-debug

If that all worked, you should wind up with an apk in `.../UmbrellaToday/target//umbrella-today-0.1.apk`

Ownerships
----------

Background image from http://www.flickr.com/photos/protectorrr/2406577829/

Umbrella Today Web app by thoughtbot

Original Java version written at a Boston Android hackfest; typed by Matt Horan.

Rewritten in Scala (with Positronic Net) by Robert Thau
