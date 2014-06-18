package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.Model
import language.experimental.macros
import reflect.macros.whitebox.Context

object ModelGeneratorMacro {

  def generate[T]: Model = macro generateImpl[T]

  def generateImpl[T : c.WeakTypeTag](c: Context): c.Expr[Model] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    new Processor[c.type](c).processType(tpe)
  }

  private class Processor[C <: Context](val c: C) {
    def processType(tpe: c.Type, filterDependentTypes: Set[c.Type] = Set.empty[c.Type]): c.Expr[Model] = {
      import c.universe._

      val fields = tpe.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
      }.get.paramLists.head

      val m = new ModelPropertyMapping[c.type](c)

      val params = fields.map { field =>
        val fieldName = field.name.toString

        val fieldMG = m.selectFor(field.typeSignature)

        q"$fieldName -> ${fieldMG.toModelProperty}"
      }

      val modelName = tpe.typeSymbol.name.toString

      val dependentTypes =
        fields
          .map(field => m.selectFor(field.typeSignature).dependentTypes)
          .foldLeft(Set.empty[c.Type])((s1, s2) => s1 ++ s2)
          .filterNot(dependentType => dependentType =:= tpe)
          .--(filterDependentTypes)

      val generateDependentTypes = dependentTypes.map(dependentType => processType(dependentType, dependentTypes ++ Set(tpe)))

      c.Expr[Model] {
        q""" {
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
              ..$generateDependentTypes
              registeredModel
          }
        }
        """
      }
    }
  }
}
