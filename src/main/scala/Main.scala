
import ar.com.crypticmind.swagger.modelgen.{DataTypeMapper, ModelRegister}
import ar.com.crypticmind.swagger.modelgen.ModelGeneratorMacro._
import com.wordnik.swagger.model.{ModelProperty, Model}

object Main extends App {

    import reflect.runtime.universe._

  object AddressTypes extends Enumeration {
    val Home, Work = Value
  }

  implicit val modelRegister = new ModelRegister {
    val registry: scala.collection.mutable.HashMap[String, Model] = scala.collection.mutable.HashMap.empty
    def get(id: String) = registry.get(id)
    def register(model: Model) = {
      registry.get(model.id) match {
        case Some(existingModel) => println(s"Skipping already registered model ${model.id}")
        case None =>
          registry.put(model.id, model)
          println(s"Registered model $model")
      }
      model
    }
  }

  implicit def optionDataTypeMapper[T : TypeTag] = new DataTypeMapper[Option[T]] {
    def toModelProperty(fieldName: String) = {
      val dtm = DataTypeMapper.dataTypeMapperFor[T]
//      if (dtm.generateModel)
//        generate[T]
      dtm.toModelProperty(fieldName).copy(required = false)
    }
    val generateModel = false
  }

  case class Address(line1: String, city: String, addressType: AddressTypes.Value)
  case class Contact(phone: String, email: String)
  case class Person(name: String, age: Int, address: Address, contact: Option[Contact])

  val m = generate[Person]
  println(s"Generation returned model: $m")

}
