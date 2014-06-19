package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.{ModelRef, AllowableListValues, AnyAllowableValues, Model}
import org.scalatest.{ShouldMatchers, WordSpec}
import ar.com.crypticmind.swagger.modelgen.WordnikModelGeneratorMacro._

class WordnikModelGenerationMacroSpec extends WordSpec with ShouldMatchers {

  trait MapBacked {
    def registry: scala.collection.mutable.HashMap[String, Model]
  }

  def defaultRegister = new WordnikModelRegister with MapBacked {
    val registry: scala.collection.mutable.HashMap[String, Model] = scala.collection.mutable.HashMap.empty

    def get(id: String) = registry.get(id)

    def register(model: Model) = {
      registry.put(model.id, model)
      model
    }
  }

  "An all-scalar class" when {

    class Simple(s1: String, s2: Int)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[Simple]

      "indicate its type" in {

        model should have(
          'id("Simple"),
          'name("Simple"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.Simple")
        )
      }

      "list its basic-type properties" in {

        model.properties.keySet should equal(Set("s1", "s2"))
        model.properties("s1") should have(
          'type("string"),
          'qualifiedType("java.lang.String"),
          'required(true),
          'allowableValues(AnyAllowableValues),
          'items(None)
        )
        model.properties("s2") should have(
          'type("int"),
          'qualifiedType("scala.Int"),
          'required(true),
          'allowableValues(AnyAllowableValues),
          'items(None)
        )
      }

      "be included in the model registry" in {

        modelRegister.registry.keySet should be (Set("Simple"))
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
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.Compound")
        )
      }

      "list its properties" in {

        model.properties.keySet should equal(Set("c1", "c2", "c3"))
        model.properties("c1") should have(
          'type("string"),
          'qualifiedType("java.lang.String"),
          'required(true),
          'allowableValues(AnyAllowableValues),
          'items(None)
        )
        model.properties("c2") should have(
          'type("int"),
          'qualifiedType("scala.Int"),
          'required(true),
          'allowableValues(AnyAllowableValues),
          'items(None)
        )
      }

      "list references to other classes" in {

        model.properties("c3") should have(
          'type("Ref"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.Ref"),
          'required(true),
          'allowableValues(AnyAllowableValues)
        )
        model.properties("c3").items should be('defined)
        model.properties("c3").items.get should have(
          'type("Ref")
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.registry.keySet should be (Set("Compound", "Ref"))
      }

      "generate model for referenced classes as well" which {

        val referencedModel = modelRegister.get("Ref").get

        "indicates its type" in {

          referencedModel should have(
            'id("Ref"),
            'name("Ref"),
            'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.Ref")
          )
        }

        "lists its properties" in {

          referencedModel.properties.keySet should equal(Set("r1"))
          referencedModel.properties("r1") should have(
            'type("string"),
            'qualifiedType("java.lang.String"),
            'required(true),
            'allowableValues(AnyAllowableValues),
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

        model.properties("cwov2") should have(
          'type("int"),
          'qualifiedType("scala.Int"),
          'required(false),
          'allowableValues(AnyAllowableValues),
          'items(None)
        )
        model.properties("cwov3") should have(
          'type("string"),
          'qualifiedType("java.lang.String"),
          'required(false),
          'allowableValues(AnyAllowableValues),
          'items(None)
        )
        model.properties("cwov4") should have(
          'type("RefOpt"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.RefOpt"),
          'required(false),
          'allowableValues(AnyAllowableValues),
          'items(Some(ModelRef(`type` = "RefOpt")))
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.registry.keySet should be (Set("ClassWithOptionalValue", "RefOpt"))
      }
    }
  }

  "A class with a Scala stock Enumeration property" when {

    class ClassWithEnumValue(cwev1: String, cwev2: SampleEnum.Value)

    "converted to Swagger model" should {

      implicit val modelRegister = defaultRegister
      val model = generate[ClassWithEnumValue]

      "indicate the enum property as string" in {

        model.properties("cwev2") should have(
          'type("string"),
          'qualifiedType("java.lang.String"),
          'required(true),
          'items(None)
        )
      }

      "list the possible values of the enum property" in {

        val expected = List("A", "B", "C")
        model.properties("cwev2").allowableValues match {
          case v: AllowableListValues => v.values should be(expected)
          case other => fail(s"Unexpected value $other, expecting $expected")
        }
      }

      "be included in the model registry with no other references" in {

        modelRegister.registry.keySet should be (Set("ClassWithEnumValue"))
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

        model.properties("cwiv2") should have(
          'type("array"),
          'qualifiedType("scala.collection.Iterable"),
          'required(true)
        )
        model.properties("cwiv3") should have(
          'type("array"),
          'qualifiedType("scala.collection.Iterable"),
          'required(true)
        )
        model.properties("cwiv4") should have(
          'type("array"),
          'qualifiedType("scala.collection.Iterable"),
          'required(true)
        )
      }

      "reference the type of the elements in the Iterables" in {

        model.properties("cwiv2").items should be('defined)
        model.properties("cwiv2").items.get should have(
          'type("int")
        )
        model.properties("cwiv3").items should be('defined)
        model.properties("cwiv3").items.get should have(
          'type("string")
        )
        model.properties("cwiv4").items should be('defined)
        model.properties("cwiv4").items.get should have(
          'type("RefIter")
        )
      }

      "be included in the model registry along with referenced classes" in {

        modelRegister.registry.keySet should be (Set("ClassWithIterableValue", "RefIter"))
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

        model.properties("cc1") should have(
          'type("string"),
          'qualifiedType("java.lang.String"),
          'required(true)
        )
        model.properties("cc2") should have(
          'type("CircularClass"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.CircularClass"),
          'required(true)
        )
        model.properties("cc3") should have(
          'type("CircularClass"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.CircularClass"),
          'required(false)
        )
        model.properties("cc4") should have(
          'type("array"),
          'qualifiedType("scala.collection.Iterable"),
          'required(true)
        )
        model.properties("cc5") should have(
          'type("RefCirc"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.RefCirc"),
          'required(true)
        )
      }

      "indicate the references to itself" in {

        model.properties("cc2").items should be('defined)
        model.properties("cc2").items.get should have(
          'type("CircularClass")
        )
        model.properties("cc3").items should be('defined)
        model.properties("cc3").items.get should have(
          'type("CircularClass")
        )
        model.properties("cc4").items should be('defined)
        model.properties("cc4").items.get should have(
          'type("CircularClass")
        )
        model.properties("cc5").items should be('defined)
        model.properties("cc5").items.get should have(
          'type("RefCirc")
        )
      }

      "be included in the model registry" in {

        modelRegister.registry.keySet should be (Set("CircularClass", "RefCirc"))
      }

      "include in the registry the referee class to references the referrer" in {

        val referee = modelRegister.get("RefCirc").get

        referee.properties("rf1") should have(
          'type("CircularClass"),
          'qualifiedType("ar.com.crypticmind.swagger.modelgen.WordnikModelGenerationMacroSpec.CircularClass"),
          'required(true)
        )
        referee.properties("rf1").items should be('defined)
        referee.properties("rf1").items.get should have(
          'type("CircularClass")
        )
      }
    }
  }
}
