package org.bostonandroid.umbrellatoday.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("org.bostonandroid.umbrellatoday", getContext.getPackageName)
  }
}