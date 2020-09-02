package org.apache.cayenne.scaladsl.server.test.model

import org.apache.cayenne.ObjectContext
import org.apache.cayenne.scaladsl.server.test.model.auto._Article

class Article extends _Article {

}


object Article {
  val serialVersionUID: Long = 1L

  def apply(content: String)(implicit objectContext: ObjectContext = null): Article = {
    val article: Article = objectContext match {
      case context: ObjectContext  => context.newObject(classOf[Article])
      case null => new Article()
    }
    article setContent content
    article
  }

  def apply(): Article = new Article()
}
