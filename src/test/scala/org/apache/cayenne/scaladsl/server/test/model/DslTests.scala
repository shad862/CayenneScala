package org.apache.cayenne.scaladsl.server.test.model

import org.apache.cayenne.ObjectContext
import org.apache.cayenne.configuration.server.ServerRuntime.builder
import org.apache.cayenne.scaladsl.server.ObjectContextOps._
import utest._



object DslTests extends TestSuite {

  val tests: Tests = Tests {

    test ("commit") {

      implicit val ctx: ObjectContext = builder().addConfig("cayenne-project.xml").build().newContext()

      //creating the object

      Article("Lorem ipsum dolor sit amet, consectetur adipiscing elit...") commit

      //or just

      Article("Excepteur sint occaecat cupidatat non proident, sunt...") !

      //finding entities

      val seq1 = Article() find

      //or just

      val seq2 = Article() ~

      assert(seq1.length == seq2.length)
      assert(seq1.length == 2)
    }
  }
}