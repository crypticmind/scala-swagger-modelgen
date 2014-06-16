
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
    override def register(model: Model) = {
      registry.get(model.id) match {
        case Some(existingModel) => println(s"Skipping already registered model ${model.id}")
        case None =>
          registry.put(model.id, model)
          println(s"Registered model $model")
      }
      model
    }
  }

  implicit def optionDataTypeMapper[T : DataTypeMapper : TypeTag] = new OptionDataTypeMapper[T]

  class OptionDataTypeMapper[T : TypeTag] extends DataTypeMapper[Option[T]] {
    def toModelProperty(fieldName: String) = ModelProperty(`type` = "Option[]", qualifiedType = "Option[]")
    val generateModel = false
  }

  case class Address(line1: String, city: String, addressType: AddressTypes.Value)
  case class Contact(phone: String, email: String)
  case class Person(name: String, age: Int, address: Address, contact: Option[Contact])

  val m = generate[Person]
  println(s"Generation returned model: $m")



//  trait M[T] {
//    def msg: String
//  }
//
//  implicit val mInt = new M[Int] {
//    val msg = "Int"
//  }
//
//  case class A()
//
//  implicit val mA = new M[A] {
//    val msg = "A"
//  }
//
//  implicit def defaultFormat[T : TypeTag] = new M[T] {
//    override val msg = typeOf[T].typeSymbol.name.toString
//  }
//
//  implicit def optionFormat[T : M : TypeTag] = new OptionM[T]
//
//  class OptionM[T : TypeTag] extends M[Option[T]] {
//    val typeName = typeOf[T].typeSymbol.name.toString
//    override val msg = s"Option[$typeName]"
//  }
//
//  def printTheType[T : M]() {
//    println(implicitly[M[T]].msg)
//  }
//
//  printTheType[Int]()
//  printTheType[Option[A]]()

}
