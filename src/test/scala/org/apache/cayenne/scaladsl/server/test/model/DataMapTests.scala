package org.apache.cayenne.scaladsl.server.test.model

import org.apache.cayenne.DataChannel
import org.apache.cayenne.access.{DataDomain, DataRowStoreFactory}
import org.apache.cayenne.configuration.server.ServerRuntime.builder
import org.apache.cayenne.di.{Inject, Provider}
import utest._


object DataMapTests extends TestSuite {

  type ChannelSupplier = () => DataChannel

  trait DataDomainProvider extends Provider[DataDomain] {
    @Inject var dataRowStoreFactory: DataRowStoreFactory = _
  }

  val tests: Tests = Tests {

    test ("DataMap loader") {

      val rnt = builder()
        .addConfig("cayenne-project.xml")
        .addModule(binder => binder.bind(classOf[DataDomain]).toProviderInstance(
          new DataDomainProvider() {
            override def get(): DataDomain = {
              val dd = new DataDomain("Manual DataDomain")
              dd.setDataRowStoreFactory(dataRowStoreFactory)
              dd
            }

          })
        ).build()

      val ctx = rnt.newContext

      println(ctx)
    }
  }
}
