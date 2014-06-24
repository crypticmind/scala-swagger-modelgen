package ar.com.crypticmind.swagger.modelgen

import ar.com.crypticmind.swagger.modelgen.ScalatraModelGeneratorMacro._
import org.joda.time.{ DateTime, LocalDateTime, LocalDate }
import org.scalatest.{ ShouldMatchers, WordSpec }
import org.scalatra.swagger.{ ModelProperty, DataType, AllowableValues, Model }
import scala.language.implicitConversions

class ScalatraModelGenerationMacroSpec extends WordSpec with ShouldMatchers {

  def defaultRegister = new ModelRegister[Model] {
    val registry: scala.collection.mutable.HashMap[String, Model] = scala.collection.mutable.HashMap.empty
    def get(id: String) = registry.get(id)
    def register(model: Model) = {
      registry.put(model.id, model)
      model
    }
    def names = registry.keySet.toSet
  }

  // A little bit of syntactic sugar
  case class ModelPropertyExtractor(properties: List[(String, ModelProperty)]) {
    val names = properties.map(_._1)
    def get(name: String): ModelProperty = properties.find(_._1 == name).get._2
  }
  implicit def toModelPropertyExtractor(properties: List[(String, ModelProperty)]) = ModelPropertyExtractor(properties)

  "An all-scalar class" when {

    class Simple(f_String: String,
                 f_Int: Int,
                 f_Double: Double,
                 f_Float: Float,
                 f_Long: Long,
                 f_BigDecimal: BigDecimal,
                 f_Char: Char,
                 f_Short: Short,
                 f_Byte: Byte,
                 f_Boolean: Boolean,
                 f_LocalDate: LocalDate,
                 f_LocalDateTime: LocalDateTime,
                 f_DateTime: DateTime)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[Simple]

      "indicate its type" in {

        model should have(
          'id("Simple"),
          'name("Simple"),
          'qualifiedName(Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.Simple"))
        )
      }

      "list its basic-type properties" in {

        model.properties.names should equal(Seq(
          "f_String",
          "f_Int",
          "f_Double",
          "f_Float",
          "f_Long",
          "f_BigDecimal",
          "f_Char",
          "f_Short",
          "f_Byte",
          "f_Boolean",
          "f_LocalDate",
          "f_LocalDateTime",
          "f_DateTime"))

        model.properties.get("f_String") should have(
          'type(DataType.String),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Int") should have(
          'type(DataType.Int),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Double") should have(
          'type(DataType.Double),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Float") should have(
          'type(DataType.Float),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Long") should have(
          'type(DataType.Long),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_BigDecimal") should have(
          'type(DataType("decimal", None)),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Char") should have(
          'type(DataType("string", Some("char(1)"))),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Short") should have(
          'type(DataType("integer", Some("int16"))),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_Byte") should have(
          'type(DataType.Byte),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_LocalDate") should have(
          'type(DataType.Date),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_LocalDateTime") should have(
          'type(DataType.DateTime),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("f_DateTime") should have(
          'type(DataType.DateTime),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
      }

      "be included in the model registry" in {

        modelRegister.names should be(Set("Simple"))
      }
    }
  }

  "A compound class" when {

    class Ref(r1: String)
    class Compound(c1: String, c2: Int, c3: Ref)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[Compound]

      "indicate its type" in {

        model should have(
          'id("Compound"),
          'name("Compound"),
          'qualifiedName(Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.Compound"))
        )
      }

      "list its properties" in {

        model.properties.names should equal(Seq("c1", "c2", "c3"))
        model.properties.get("c1") should have(
          'type(DataType.String),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("c2") should have(
          'type(DataType.Int),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
      }

      "list references to other classes" in {

        model.properties.get("c3") should have(
          'type(DataType.ValueDataType("Ref", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.Ref"))),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.names should be(Set("Compound", "Ref"))
      }

      "generate model for referenced classes as well" which {

        val referencedModel = modelRegister.get("Ref").get

        "indicates its type" in {

          referencedModel should have(
            'id("Ref"),
            'name("Ref"),
            'qualifiedName(Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.Ref"))
          )
        }

        "lists its properties" in {

          referencedModel.properties.names should equal(Seq("r1"))
          referencedModel.properties.get("r1") should have(
            'type(DataType.String),
            'required(true),
            'allowableValues(AllowableValues.AnyValue),
            'items(None)
          )
        }
      }
    }
  }

  "A class with optional properties" when {

    class RefOpt(r1: String)
    class ClassWithOptionalValue(cwov1: String, cwov2: Option[Int], cwov3: Option[String], cwov4: Option[RefOpt])

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[ClassWithOptionalValue]

      "indicate such properties as not required" in {

        model.properties.get("cwov2") should have(
          'type(DataType.Int),
          'required(false),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("cwov3") should have(
          'type(DataType.String),
          'required(false),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
        model.properties.get("cwov4") should have(
          'type(DataType.ValueDataType("RefOpt", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.RefOpt"))),
          'required(false),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.names should be(Set("ClassWithOptionalValue", "RefOpt"))
      }
    }
  }

  "A class with a Scala stock Enumeration property" when {

    class ClassWithEnumValue(cwev1: String, cwev2: SampleEnum.Value)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[ClassWithEnumValue]

      "indicate the enum property as string" in {

        model.properties.get("cwev2") should have(
          'type(DataType.String),
          'required(true),
          'items(None)
        )
      }

      "list the possible values of the enum property" in {

        val expected = List("A", "B", "C")
        model.properties.get("cwev2").allowableValues match {
          case v: AllowableValues.AllowableValuesList[_] => v.values should be(expected)
          case other => fail(s"Unexpected value $other, expecting $expected")
        }
      }

      "be included in the model registry with no other references" in {

        modelRegister.names should be(Set("ClassWithEnumValue"))
      }
    }
  }

  "A class with a Scala Iterable properties" when {

    class RefIter(r1: String)
    class ClassWithIterableValue(cwiv1: String, cwiv2: List[Int], cwiv3: List[String], cwiv4: List[RefIter])

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[ClassWithIterableValue]

      "indicate the Iterable properties as array" in {

        model.properties.get("cwiv2") should have(
          'type(DataType.ContainerDataType("Array", Some(DataType.Int), uniqueItems = false)),
          'required(true)
        )
        model.properties.get("cwiv3") should have(
          'type(DataType.ContainerDataType("Array", Some(DataType.String), uniqueItems = false)),
          'required(true)
        )
        model.properties.get("cwiv4") should have(
          'type(DataType.ContainerDataType("Array", Some(DataType.ValueDataType("RefIter", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.RefIter"))), uniqueItems = false)),
          'required(true)
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.names should be(Set("ClassWithIterableValue", "RefIter"))
      }
    }
  }

  "A class with a reference to itself" when {

    class RefCirc(rf1: CircularClass)
    class CircularClass(cc1: String, cc2: CircularClass, cc3: Option[CircularClass], cc4: List[CircularClass], cc5: RefCirc)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[CircularClass]

      "indicate the properties" in {

        model.properties.get("cc1") should have(
          'type(DataType.String),
          'required(true)
        )
        model.properties.get("cc2") should have(
          'type(DataType.ValueDataType("CircularClass", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.CircularClass"))),
          'required(true)
        )
        model.properties.get("cc3") should have(
          'type(DataType.ValueDataType("CircularClass", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.CircularClass"))),
          'required(false)
        )
        model.properties.get("cc4") should have(
          'type(DataType.ContainerDataType("Array", Some(DataType.ValueDataType("CircularClass", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.CircularClass"))), uniqueItems = false)),
          'required(true)
        )
        model.properties.get("cc5") should have(
          'type(DataType.ValueDataType("RefCirc", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.RefCirc"))),
          'required(true)
        )
      }

      "be included in the model registry" in {

        modelRegister.names should be(Set("CircularClass", "RefCirc"))
      }

      "include in the registry the referee class to references the referrer" in {

        val referee = modelRegister.get("RefCirc").get

        referee.properties.get("rf1") should have(
          'type(DataType.ValueDataType("CircularClass", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.CircularClass"))),
          'required(true)
        )
      }
    }
  }

  "A class with a property that is a trait" when {

    trait RefTrait {
      val r1: String
    }
    class ClassWithATraitProperty(cwatp1: String, cwatp2: RefTrait)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[ClassWithATraitProperty]

      "indicate its type" in {

        model should have(
          'id("ClassWithATraitProperty"),
          'name("ClassWithATraitProperty"),
          'qualifiedName(Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.ClassWithATraitProperty"))
        )
      }

      "list its properties" in {

        model.properties.names should equal(Seq("cwatp1", "cwatp2"))
      }

      "have a reference to the trait" in {

        model.properties.get("cwatp2") should have(
          'type(DataType.ValueDataType("RefTrait", None, Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.RefTrait"))),
          'required(true),
          'allowableValues(AllowableValues.AnyValue),
          'items(None)
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.names should be(Set("ClassWithATraitProperty", "RefTrait"))
      }

      "generate model for referenced classes as well" which {

        val referencedModel = modelRegister.get("RefTrait").get

        "indicates its type" in {

          referencedModel should have(
            'id("RefTrait"),
            'name("RefTrait"),
            'qualifiedName(Some("ar.com.crypticmind.swagger.modelgen.ScalatraModelGenerationMacroSpec.RefTrait"))
          )
        }

        "list no properties" in {

          referencedModel.properties.names should be('empty)
        }
      }
    }
  }

}
