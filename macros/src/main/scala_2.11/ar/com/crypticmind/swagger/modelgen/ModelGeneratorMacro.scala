package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.Model

trait ModelRegister {
  def register(model: Model): Model
}

object ModelGeneratorMacro {

  import language.experimental.macros
  import reflect.macros.whitebox.Context

  def generate[T]: Model = macro generateImpl[T]

  def generateImpl[T : c.WeakTypeTag](c: Context): c.Expr[Model] = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
    }.get.paramLists.head

    val params = fields.map { field =>
      val fieldName = field.name.toString

      val fieldType =
        q""" {
          val dataTypeMapper = ar.com.crypticmind.swagger.modelgen.DataTypeMapper.dataTypeMapperFor[${field.typeSignature}]
          if (dataTypeMapper.generateModel)
            generate[${field.typeSignature}]
          dataTypeMapper.toModelProperty($fieldName)
        }
        """

      q"$fieldName -> $fieldType"
    }

    val modelName = tpe.typeSymbol.name.toString

    c.Expr[Model] {
      q"""
        val model =
          com.wordnik.swagger.model.Model(
            id = $modelName,
            name = $modelName,
            qualifiedType = $modelName,
            properties = scala.collection.mutable.LinkedHashMap(..$params))
        implicitly[ModelRegister].register(model)
      """
    }
  }
}
