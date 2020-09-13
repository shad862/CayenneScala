package org.apache.cayenne.scaladsl.server.test.model

import java.util

import org.apache.cayenne.{ObjectContext, PersistenceState}
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy
import org.apache.cayenne.access.translator.batch.BatchTranslatorFactory
import org.apache.cayenne.access.translator.select.SelectTranslatorFactory
import org.apache.cayenne.access.{DataDomain, DataNode, DataRowStoreFactory}
import org.apache.cayenne.configuration.DataNodeDescriptor
import org.apache.cayenne.configuration.server.DbAdapterFactory
import org.apache.cayenne.configuration.server.ServerRuntime.builder
import org.apache.cayenne.datasource.DriverDataSource
import org.apache.cayenne.di.{AdhocObjectFactory, Inject, Module, Provider}
import org.apache.cayenne.log.JdbcEventLogger
import org.apache.cayenne.map._
import org.apache.derby.jdbc.EmbeddedDriver
import utest._

object DataMapManualMappingTest extends TestSuite {

  trait DataDomainProvider extends Provider[DataDomain] {
    @Inject var dataRowStoreFactory: DataRowStoreFactory = _
    @Inject var dataMap: DataMap = _
    @Inject var adapterFactory: DbAdapterFactory = _
    @Inject var adhocObjectFactory: AdhocObjectFactory = _
    @Inject var jdbcEventLogger: JdbcEventLogger = _
    @Inject var entitySorter: EntitySorter = _
    @Inject var batchTranslatorFactory: BatchTranslatorFactory = _
    @Inject var selectTranslatorFactory: SelectTranslatorFactory = _
  }

  class ManualDataDomainProvider extends DataDomainProvider {
    override def get(): DataDomain = {
      val dd = new DataDomain("Manual DataDomain")
      dd.setDataRowStoreFactory(dataRowStoreFactory)
      dd.addDataMap(dataMap)
      dd.addNode({
        val dn = new DataNode()
        dn.setName("Manual DataNode")
        dn.setDataSourceFactory("org.apache.cayenne.configuration.server.XMLPoolingDataSourceFactory")
        dn.setSchemaUpdateStrategy(new CreateIfNoSchemaStrategy())

        val source = new DriverDataSource(new EmbeddedDriver(), "jdbc:derby:memory:testdb;create=true", null, null)
        dn.setDataSource(source)
        dn.addDataMap(dataMap)

        val adapter = adapterFactory.createAdapter(new DataNodeDescriptor(), source)
        dn.setAdapter(adapter)

        dn.setBatchTranslatorFactory(batchTranslatorFactory)
        dn.setSelectTranslatorFactory(selectTranslatorFactory)
        dn.setJdbcEventLogger(jdbcEventLogger)

        dn
      })
      entitySorter.setEntityResolver(new EntityResolver(util.Collections.singletonList(dataMap)))
      dd.setEntitySorter(entitySorter)
      dd
    }
  }

  trait DataMapProvider extends Provider[DataMap] {
    @Inject var dbs: util.List[DbEntity] = _
    @Inject var obs: util.List[ObjEntity] = _
  }

  class ManualDataMapProvider extends DataMapProvider {
    override def get(): DataMap = {
      val dm = new DataMap()
      dm.setName("Manual DataMap")
      dbs.forEach(dm.addDbEntity(_))
      obs.forEach(dm.addObjEntity(_))
      dm
    }
  }

  val dataDomainProviderModule: Module =
    binder =>
      binder.bind(classOf[DataDomain]).toProvider(classOf[ManualDataDomainProvider])

  val dataMapProviderModule: Module =
    binder =>
      binder.bind(classOf[DataMap]).toProvider(classOf[ManualDataMapProvider])

  val entitiesProviderModule: Module = binder => {
    val dbEntity = new DbEntity()
    dbEntity.setName("Article")
    dbEntity.addAttribute({
      val dbAttribute = new DbAttribute()
      dbAttribute.setName("id")
      dbAttribute.setType(java.sql.Types.INTEGER)
      dbAttribute.setPrimaryKey(true)
      dbAttribute
    })
    dbEntity.addAttribute({
      val dbAttribute = new DbAttribute()
      dbAttribute.setName("content")
      dbAttribute.setType(java.sql.Types.CLOB)
      dbAttribute
    })
    dbEntity.setPrimaryKeyGenerator(new DbKeyGenerator())

    val objEntity = new ObjEntity()
    objEntity.setClassName("org.apache.cayenne.scaladsl.server.test.model.Article")
    objEntity.setDbEntityName("Article")
    //todo: add callbacks
    objEntity.setName("Article")
    objEntity.addAttribute({
      val objAttr = new ObjAttribute()
      objAttr.setName("content")
      objAttr.setType("java.lang.String")
      objAttr.setDbAttributePath("content")
      objAttr
    })

    binder.bindList(classOf[DbEntity]).add(dbEntity)
    binder.bindList(classOf[ObjEntity]).add(objEntity)
  }

  val tests: Tests = Tests {
    test ("DataMap loader") {

      System.setSecurityManager(null)

      val rnt = builder()
        //.addConfig("cayenne-project.xml") //disable default xml mapping
        .addModule(dataDomainProviderModule)
        //next line hide previous DataDomainProvider
        //.dataSource(url("jdbc:derby:memory:testdb;create=true").driver("org.apache.derby.jdbc.EmbeddedDriver").build())
        .addModule(dataMapProviderModule)
        .addModule(entitiesProviderModule)
        .build()

      val ctx = rnt.newContext
      val article = ctx.newObject(classOf[Article])
      article.setContent("Content")
      ctx.commitChanges()

      assert(article.getPersistenceState == PersistenceState.COMMITTED)
    }
  }
}