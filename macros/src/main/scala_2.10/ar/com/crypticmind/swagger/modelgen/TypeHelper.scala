package ar.com.crypticmind.swagger.modelgen

import scala.reflect.macros.Context

class TypeHelper[C <: Context](val c: C) {
  import c.universe._

  def getPath(t: c.Type): List[String] =
    if (t.typeSymbol == c.universe.rootMirror.RootClass)
      Nil
    else
      t.typeSymbol.asType.name.decoded :: getPath(t.asInstanceOf[TypeRef].pre.typeSymbol)

  def getPath(s: Symbol): List[String] =
    if (s == c.universe.rootMirror.RootClass)
      Nil
    else
      s.name.decoded :: getPath(s.owner)

  def tree(path: List[String], t: c.Tree = EmptyTree): c.Tree =
    if (t == EmptyTree)
      tree(path.tail, Ident(newTermName(path.head)))
    else
      path match {
        case Nil => EmptyTree
        case head :: Nil => Select(t, newTypeName(head))
        case head :: tail => tree(tail, Select(t, newTermName(head)))
      }

}
