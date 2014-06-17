
import ar.com.crypticmind.swagger.modelgen.ModelGeneratorMacro._
import ar.com.crypticmind.swagger.modelgen.ModelRegister
import com.wordnik.swagger.model.Model

object Main extends App {

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

  case class Address(line1: String, city: String, addressType: AddressTypes.Value)
  case class Contact(phone: String, email: String)
  case class Person(name: String, age: Int, address: Address, contact: Option[Contact])

  val m = generate[Person]
  println(s"Generation returned model: $m")

}
