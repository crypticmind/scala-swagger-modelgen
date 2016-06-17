package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.ModelProperty
import scala.reflect.macros.whitebox.Context

class WordnikModelPropertyMapping[C <: Context](val c: C) {
  import c.universe._

  abstract class ModelPropertyGenerator {
    def toModelProperty: c.Expr[ModelProperty]
    def dependentTypes: Set[c.Type]
  }

  object StringModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "StringModelPropertyGenerator"
    val toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = "string",
            qualifiedType = "java.lang.String",
            required = true)
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object IntModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "IntModelPropertyGenerator"
    val toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = "int",
            qualifiedType = "scala.Int",
            required = true)
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  class ObjectModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectName = t.typeSymbol.name.toString
    val qualifiedType = t.typeSymbol.asClass.fullName
    override val toString = "ObjectModelPropertyGenerator"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = $objectName,
            qualifiedType = $qualifiedType,
            required = true,
            items = Some(com.wordnik.swagger.model.ModelRef(`type` = $objectName)))
        """
      }
    val dependentTypes = Set(t)
  }

  class OptionModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectType = t.dealias.typeArgs.head.typeSymbol.asClass.fullName
    val mapperForType = selectFor(t.dealias.typeArgs.head)
    override val toString = s"OptionModelPropertyGenerator(${mapperForType.toString})"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q" ${mapperForType.toModelProperty}.copy(required = false) "
      }
    val dependentTypes = mapperForType.dependentTypes
  }

  class EnumModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    override val toString = s"EnumModelPropertyGenerator(${t.typeSymbol.owner})"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q""" {
          import reflect.runtime.universe._
          import reflect.runtime._
          val tpe = typeOf[$t].asInstanceOf[TypeRef]
          val pre = tpe.pre
          val mod = pre.termSymbol.asModule
          val modMirror = currentMirror.reflectModule(mod)
          val modInst = modMirror.instance
          val values = modInst.asInstanceOf[Enumeration].values.toList.map(_.toString)
          com.wordnik.swagger.model.ModelProperty(
            `type` = "string",
            qualifiedType = "java.lang.String",
            required = true,
            allowableValues = com.wordnik.swagger.model.AllowableListValues(values))
        }
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  class IterModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectType = t.dealias.typeArgs.head.typeSymbol.asClass.fullName
    val mapperForType = selectFor(t.dealias.typeArgs.head)
    override val toString = s"IterModelPropertyGenerator(${mapperForType.toString})"

    def toModelProperty = {
      c.Expr[ModelProperty] {
        q""" {
          val refType = ${mapperForType.toModelProperty}.`type`
          com.wordnik.swagger.model.ModelProperty(
            `type` = "array",
            qualifiedType = "scala.collection.Iterable",
            required = true,
            items = Some(com.wordnik.swagger.model.ModelRef(`type` = refType)))
        }
        """
      }
    }
    val dependentTypes = mapperForType.dependentTypes
  }

  def selectFor(t: c.Type): ModelPropertyGenerator = t match {
    case str if t <:< c.typeOf[String] => StringModelPropertyGenerator
    case int if t <:< c.typeOf[Int] => IntModelPropertyGenerator
    case opt if t <:< c.typeOf[Option[_]] => new OptionModelPropertyGenerator(opt)
    case enm if t <:< c.typeOf[Enumeration#Value] => new EnumModelPropertyGenerator(enm)
    case itr if t <:< c.typeOf[Iterable[_]] => new IterModelPropertyGenerator(itr)
    case other => new ObjectModelPropertyGenerator(other)
  }

}
