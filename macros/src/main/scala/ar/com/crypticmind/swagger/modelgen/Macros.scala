package ar.com.crypticmind.swagger.modelgen

object Model {

  import reflect.runtime.universe._
  import reflect.runtime._

  object BasicModels {

    implicit val stringDataTypeMapper = new DataTypeMapper[String] {
      def toModelProperty(fieldName: String) = ModelProperty(fieldName, DataTypes.String)
      val generateModel = false
      override val toString = "DataTypeMapper[String]"
    }

    implicit val intDataTypeMapper = new DataTypeMapper[Int]{
      def toModelProperty(fieldName: String) = ModelProperty(fieldName, DataTypes.Int)
      val generateModel = false
      override val toString = "DataTypeMapper[Int]"
    }

    implicit def enumValueDataTypeMapper[EV <: Enumeration#Value : TypeTag] = new DataTypeMapper[EV]{

      val tpe = typeOf[EV].asInstanceOf[TypeRef]
      val pre = tpe.pre
      val m = pre.termSymbol.asModule
      val mm = currentMirror.reflectModule(m)
      val im = mm.instance
      val values = im.asInstanceOf[Enumeration].values.toSeq.map(_.toString)

      def toModelProperty(fieldName: String) = ModelProperty(fieldName, DataTypes.Enum, allowedValues = values)
      val generateModel = false
      override val toString = "DataTypeMapper[EV <: Enumeration#Value]"
    }

    def objectDataTypeMapper[T : TypeTag] = new DataTypeMapper[T] {
      val referencedModel = typeOf[T].typeSymbol.name.toString
      def toModelProperty(fieldName: String) = ModelProperty(fieldName, DataTypes.Object, Some(ModelReference(referencedModel)))
      val generateModel = true
      override val toString = "DataTypeMapper[T]"
    }

  }

  object DataTypes extends Enumeration {
    val String, Int, Boolean, Enum, Object = Value
  }

  case class ModelReference(id: String)

  case class ModelProperty(name: String, `type`: DataTypes.Value, reference: Option[ModelReference] = None, allowedValues: Seq[String] = Nil)

  case class Model(id: String, properties: Map[String, ModelProperty])

  trait DataTypeMapper[T] {
    def toModelProperty(fieldName: String): ModelProperty
    def generateModel: Boolean
  }

  def dataTypeMapperFor[T](implicit tt: TypeTag[T], dataTypeMapper: DataTypeMapper[T] = null) = {
    if (dataTypeMapper != null)
      dataTypeMapper
    else
      BasicModels.objectDataTypeMapper[T](tt)
  }

  def register(m: Model) {
    println(s"Registering model $m")
  }
}


object GenMacro {

  import language.experimental.macros
  import reflect.macros.whitebox.Context

  def generate[T](): Unit = macro generateImpl[T]

  def generateImpl[T : c.WeakTypeTag](c: Context)(): c.Expr[Unit] = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
    }.get.paramLists.head

    val params = fields.map { field =>
      val fieldName = field.name.toString

      val fieldType =
        q""" {
          val dataTypeMapper = ar.com.crypticmind.swagger.modelgen.Model.dataTypeMapperFor[${field.typeSignature}]
          if (dataTypeMapper.generateModel)
            generate[${field.typeSignature}]()
          dataTypeMapper.toModelProperty($fieldName)
        }
        """

      q"$fieldName -> $fieldType"
    }

    val modelName = tpe.typeSymbol.name.toString

    c.Expr[Unit] {
      q"""
        ar.com.crypticmind.swagger.modelgen.Model.register(ar.com.crypticmind.swagger.modelgen.Model.Model($modelName, Map(..$params)))
      """
    }

  }
}
