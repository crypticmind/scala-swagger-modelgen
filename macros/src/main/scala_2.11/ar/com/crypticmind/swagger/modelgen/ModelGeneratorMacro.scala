package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.{ModelProperty, Model}
import language.experimental.macros
import reflect.macros.whitebox.Context

trait ModelRegister {
  def get(id: String): Option[Model]
  def register(model: Model): Model
}

class ModelGeneratorMapper[C <: Context](val c: C) {

  abstract class MG {
    def toModelProperty: c.Expr[ModelProperty]
    def dependentTypes: Set[c.Type]
  }

  object StringMG extends MG {
    import c.universe._
    override val toString = "StringMG"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = "string",
            qualifiedType = "java.lang.String",
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object IntMG extends MG {
    import c.universe._
    override val toString = "IntMG"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = "int",
            qualifiedType = "scala.Int",
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  class ObjectMG(t: c.Type) extends MG {
    import c.universe._
    val referencedType = t.typeSymbol.asClass.fullName
    override val toString = s"ObjectMG($referencedType)"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = $referencedType,
            qualifiedType = $referencedType,
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set(t)
  }

  class OptionMG(t: c.Type) extends MG {
    import c.universe._
    val referencedType = t.dealias.typeArgs.head.typeSymbol.asClass.fullName
    override val toString = s"OptionMG($referencedType)"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = $referencedType,
            qualifiedType = $referencedType,
            required = false,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set(t.dealias.typeArgs.head)
  }

  class EnumMG(t: c.Type) extends MG {
    import c.universe._

    //    val tpe = typeOf[EV].asInstanceOf[TypeRef]
    //    val pre = tpe.pre
    //    val m = pre.termSymbol.asModule
    //    val mm = currentMirror.reflectModule(m)
    //    val im = mm.instance
    //    val values = im.asInstanceOf[Enumeration].values.toList.map(_.toString)

//    println(s"EEE => ${t.typeSymbol.owner}")

    override val toString = s"EnumMG(${t.typeSymbol.owner})"
    val mappedBy = s"Mapped by ${this.toString}"
    def toModelProperty =
      c.Expr[ModelProperty] {
        q"""
          com.wordnik.swagger.model.ModelProperty(
            `type` = "string",
            qualifiedType = "java.lang.String",
            required = true,
            description = Some($mappedBy))
        """
      }
    val dependentTypes = Set.empty[c.Type]
  }

  def selectFor(t: c.Type) = t match {
    case str if t <:< c.typeOf[String] => StringMG
    case int if t <:< c.typeOf[Int] => IntMG
    case opt if t <:< c.typeOf[Option[_]] => new OptionMG(opt)
    case enm if t <:< c.typeOf[Enumeration#Value] => new EnumMG(enm)
    case other => new ObjectMG(other)
  }

//  def gatherDependentTypes(t: c.Type): Set[c.Type] = {
//    import c.universe._
//    val ownType = Set(t)
//
//    val typeParams = t.dealias.typeArgs.toSet
//
//    val fieldTypes = t.decls.collectFirst {
//      case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
//    }.get.paramLists.head.map(fieldType => gatherDependentTypes(fieldType.typeSignature)).foldLeft(Set.empty[c.Type])((s1, s2) => s1 ++ s2)
//
//    ownType ++ typeParams ++ fieldTypes
//  }
}

object ModelGeneratorMacro {

  def generate[T]: Model = macro generateImpl[T]

  def generateImpl[T : c.WeakTypeTag](c: Context): c.Expr[Model] = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
    }.get.paramLists.head

    val m = new ModelGeneratorMapper[c.type](c)

    val params = fields.map { field =>
      val fieldName = field.name.toString

//      val fieldType =
//        q"""
//          ar.com.crypticmind.swagger.modelgen.DataTypeMapper.dataTypeMapperFor[${field.typeSignature}].toModelProperty($fieldName)
//        """

      val fieldMG = m.selectFor(field.typeSignature)

      q"$fieldName -> ${fieldMG.toModelProperty}"
    }

    val modelName = tpe.typeSymbol.name.toString

    val dependentTypes =
      fields
        .map(field => m.selectFor(field.typeSignature).dependentTypes)
        .foldLeft(Set.empty[c.Type])((s1, s2) => s1 ++ s2)
        .map(dependentType => q" generate[$dependentType] ")

    val result = c.Expr[Model] {
      q"""
          implicitly[ar.com.crypticmind.swagger.modelgen.ModelRegister].get($modelName) match {
            case Some(existingModel) =>
              existingModel
            case None =>
              val model =
                com.wordnik.swagger.model.Model(
                  id = $modelName,
                  name = $modelName,
                  qualifiedType = $modelName,
                  properties = scala.collection.mutable.LinkedHashMap(..$params))
              val registeredModel = implicitly[ar.com.crypticmind.swagger.modelgen.ModelRegister].register(model)
              ..$dependentTypes
              registeredModel
          }
      """
    }
//    println(result)
    result
  }


}
