package ar.com.crypticmind.swagger.modelgen

import org.joda.time.{LocalDate, LocalDateTime, DateTime}
import org.scalatra.swagger.ModelProperty
import reflect.macros.whitebox.Context

class ScalatraModelPropertyMapping[C <: Context](val c: C) {
  import c.universe._

  abstract class ModelPropertyGenerator {
    def toModelProperty: c.Expr[ModelProperty]
    def dependentTypes: Set[c.Type]
  }

  object StringModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "StringModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.String,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object IntModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "IntModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Int,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object DoubleModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "DoubleModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Double,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object FloatModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "FloatModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Float,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object LongModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "LongModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Long,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object BigDecimalModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "BigDecimalModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType("decimal", None),
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object CharModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "CharModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType("string", Some("char(1)")),
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object ShortModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "ShortModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType("integer", Some("int16")),
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object ByteModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "ByteModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Byte,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object BooleanModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "BooleanModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Boolean,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object LocalDateModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "LocalDateModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.Date,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object LocalDateTimeModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "(Local)DateTimeModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.DateTime,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  class ObjectModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectName = t.typeSymbol.name.toString
    val qualifiedName = t.typeSymbol.asClass.fullName
    override val toString = "ObjectModelPropertyGenerator"
    val mappedBy = s"Mapped by ${this.toString}($objectName)"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.ValueDataType($objectName, None, Some($qualifiedName)),
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set(t)
  }

  class OptionModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectType = t.dealias.typeArgs.head.typeSymbol.asClass.fullName
    val mapperForType = selectFor(t.dealias.typeArgs.head)
    override val toString = s"OptionModelPropertyGenerator(${mapperForType.toString})"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q" ${mapperForType.toModelProperty}.copy(required = false, description = Some($mappedBy)) "
      }
    val dependentTypes = mapperForType.dependentTypes
  }

  class EnumModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    override val toString = s"EnumModelPropertyGenerator(${t.typeSymbol.owner})"
    val mappedBy = s"Mapped by ${this.toString}"
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
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.String,
            required = true,
            description = Some($mappedBy),
            allowableValues = org.scalatra.swagger.AllowableValues.AllowableValuesList(values))
        }
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  class IterModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectType = t.dealias.typeArgs.head.typeSymbol.asClass.fullName
    val mapperForType = selectFor(t.dealias.typeArgs.head)
    override val toString = s"IterModelPropertyGenerator(${mapperForType.toString})"
    val mappedBy = s"Mapped by ${this.toString}"

    def toModelProperty = {
      c.Expr[ModelProperty] {
        q""" {
          val refType = ${mapperForType.toModelProperty}.`type`
          org.scalatra.swagger.ModelProperty(
            `type` = org.scalatra.swagger.DataType.ContainerDataType("Array", Some(refType), uniqueItems = false),
            required = true,
            description = Some($mappedBy))
        }
        """
      }
    }
    val dependentTypes = mapperForType.dependentTypes
  }

  def selectFor(t: c.Type): ModelPropertyGenerator = t match {
    case str if t <:< c.typeOf[String] => StringModelPropertyGenerator
    case int if t <:< c.typeOf[Int] => IntModelPropertyGenerator
    case dbl if t <:< c.typeOf[Double] => DoubleModelPropertyGenerator
    case flt if t <:< c.typeOf[Float] => FloatModelPropertyGenerator
    case lng if t <:< c.typeOf[Long] => LongModelPropertyGenerator
    case lng if t <:< c.typeOf[BigDecimal] => BigDecimalModelPropertyGenerator
    case chr if t <:< c.typeOf[Char] => CharModelPropertyGenerator
    case shr if t <:< c.typeOf[Short] => ShortModelPropertyGenerator
    case bte if t <:< c.typeOf[Byte] => ByteModelPropertyGenerator
    case bln if t <:< c.typeOf[Boolean] => BooleanModelPropertyGenerator
    case dte if t <:< c.typeOf[LocalDate] => LocalDateModelPropertyGenerator
    case ldt if t <:< c.typeOf[LocalDateTime] => LocalDateTimeModelPropertyGenerator
    case dti if t <:< c.typeOf[DateTime] => LocalDateTimeModelPropertyGenerator
    case opt if t <:< c.typeOf[Option[_]] => new OptionModelPropertyGenerator(opt)
    case enm if t <:< c.typeOf[Enumeration#Value] => new EnumModelPropertyGenerator(enm)
    case itr if t <:< c.typeOf[Iterable[_]] => new IterModelPropertyGenerator(itr)
    case other => new ObjectModelPropertyGenerator(other)
  }

}
