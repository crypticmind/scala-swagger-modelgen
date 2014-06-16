package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model._

trait DataTypeMapper[T] {
  def toModelProperty(fieldName: String): ModelProperty
  def generateModel: Boolean
}

object DataTypeMapper {

  import reflect.runtime.universe._
  import reflect.runtime._

  implicit val stringDataTypeMapper = new DataTypeMapper[String] {
    def toModelProperty(fieldName: String) =
      ModelProperty(
        `type` = "string",
        qualifiedType = "java.lang.String",
        required = true,
        description = Some(s"Mapped by ${this.toString}"))
    val generateModel = false
    override val toString = "DataTypeMapper[String]"
  }

  implicit val intDataTypeMapper = new DataTypeMapper[Int]{
    def toModelProperty(fieldName: String) =
      ModelProperty(
        `type` = "int",
        qualifiedType = "scala.Int",
        required = true,
        description = Some(s"Mapped by ${this.toString}"))
    val generateModel = false
    override val toString = "DataTypeMapper[Int]"
  }

  implicit def enumValueDataTypeMapper[EV <: Enumeration#Value : TypeTag] = new DataTypeMapper[EV]{

    val tpe = typeOf[EV].asInstanceOf[TypeRef]
    val pre = tpe.pre
    val m = pre.termSymbol.asModule
    val mm = currentMirror.reflectModule(m)
    val im = mm.instance
    val values = im.asInstanceOf[Enumeration].values.toList.map(_.toString)

    def toModelProperty(fieldName: String) =
      ModelProperty(
        `type` = "string",
        qualifiedType = tpe.sym.name.toString,
        required = true,
        description = Some(s"Mapped by ${this.toString}"),
        allowableValues = AllowableListValues(values))
    val generateModel = false
    override val toString = s"DataTypeMapper[${tpe.sym.name.toString}}]"
  }

  def objectDataTypeMapper[T : TypeTag] = new DataTypeMapper[T] {
    val referencedModel = typeOf[T].typeSymbol.name.toString
    def toModelProperty(fieldName: String) =
      ModelProperty(
        `type` = referencedModel,
        qualifiedType = referencedModel,
        required = true,
        description = Some(s"Mapped by ${this.toString}"),
        items = Some(ModelRef(`type` = referencedModel)))
    val generateModel = true
    override val toString = s"DataTypeMapper[Option[$referencedModel]]"
  }

  def dataTypeMapperFor[T](implicit tt: TypeTag[T], dataTypeMapper: DataTypeMapper[T] = null) = {
    if (dataTypeMapper != null)
      dataTypeMapper
    else
      DataTypeMapper.objectDataTypeMapper[T](tt)
  }

}

//object Model {
//
//  object DataTypes extends Enumeration {
//    val String, Int, Boolean, Enum, Object = Value
//  }
//
//  case class ModelReference(id: String)
//
//  case class ModelProperty(name: String, `type`: DataTypes.Value, reference: Option[ModelReference] = None, allowedValues: Seq[String] = Nil)
//
//  case class Model(id: String, properties: Map[String, ModelProperty])
//
//}
