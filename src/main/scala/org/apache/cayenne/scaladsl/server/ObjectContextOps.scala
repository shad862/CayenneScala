package org.apache.cayenne.scaladsl.server

import org.apache.cayenne.query.ObjectSelect
import org.apache.cayenne.query.ObjectSelect.query
import org.apache.cayenne.{BaseDataObject, ObjectContext}

import scala.reflect.ClassTag

object ObjectContextOps {
  val MESSAGE_NO_OBJECT_CONTEXT_FOUND = "No ObjectContext found"
  implicit class BaseDataObjectWithCommit[A <: BaseDataObject : ClassTag](entity: A){
    def commit(implicit context: ObjectContext = null): A = {
      if (entity.getObjectContext == null) {
        val objectContext = implicitly[ObjectContext]
        if (objectContext == null) {
          throw new RuntimeException(MESSAGE_NO_OBJECT_CONTEXT_FOUND)
        }
        objectContext.localObject(entity)
        objectContext.commitChanges()
      } else {
        entity.getObjectContext.commitChanges()
      }
      entity
    }
    @inline def !(implicit context: ObjectContext): A = commit(context)
  }

  implicit class BaseObjectWithSelect[A <: BaseDataObject : ClassTag](entity: A){
    def find(implicit context: ObjectContext = null): Seq[A] = {
      val objectContext = entity.getObjectContext match {
        case context: ObjectContext => context
        case null => implicitly[ObjectContext]
      }
      if (objectContext == null) {
        throw new RuntimeException(MESSAGE_NO_OBJECT_CONTEXT_FOUND)
      }
      import scala.jdk.CollectionConverters._
      query(entity.getClass).select(objectContext).asScala.toSeq
    }
    @inline def ~(implicit context: ObjectContext): Seq[A] = find(context)
  }
}