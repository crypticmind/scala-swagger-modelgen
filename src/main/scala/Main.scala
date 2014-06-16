
object Main extends App {
	
  import ar.com.crypticmind.swagger.modelgen.GenMacro._
  import ar.com.crypticmind.swagger.modelgen.Model._

  import ar.com.crypticmind.swagger.modelgen.Model.BasicModels._

  object AddressTypes extends Enumeration {
    val Home, Work = Value
  }

  case class Address(line1: String, city: String, addressType: AddressTypes.Value)
  case class Person(name: String, age: Int, address: Address)

  generate[Person]()
}
