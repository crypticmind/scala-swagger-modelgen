package ar.com.crypticmind.swagger.modelgen

import org.scalatra.swagger.Model
import language.experimental.macros
import reflect.macros.whitebox.Context

object ScalatraModelGeneratorMacro {

  def generate[T]: Model = macro generateImpl[T]

  def generateImpl[T : c.WeakTypeTag](c: Context): c.Expr[Model] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    new Processor[c.type](c).processType(tpe)
  }

  private class Processor[C <: Context](val c: C) {
    def processType(tpe: c.Type, filterDependentTypes: Set[c.Type] = Set.empty[c.Type]): c.Expr[Model] = {
      import c.universe._

      val primaryConstructor = tpe.decls.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
      }

      val fields = primaryConstructor match {
        case Some(pc) => pc.paramLists.head
        case None => List.empty
      }

      val m = new ScalatraModelPropertyMapping[c.type](c)

      val params = fields.map { field =>
        val fieldName = field.name.toString

        val fieldMG = m.selectFor(field.typeSignature)

        q"$fieldName -> ${fieldMG.toModelProperty}"
      }

      val modelName = tpe.typeSymbol.name.toString
      val qualifiedName = tpe.typeSymbol.asClass.fullName

      val dependentTypes =
        fields
          .map(field => m.selectFor(field.typeSignature).dependentTypes)
          .foldLeft(Set.empty[c.Type])((s1, s2) => s1 ++ s2)
          .filterNot(dependentType => dependentType =:= tpe)
          .--(filterDependentTypes)

      val generateDependentTypes = dependentTypes.map(dependentType => processType(dependentType, dependentTypes ++ Set(tpe)))

      c.Expr[Model] {
        q""" {
          implicitly[ar.com.crypticmind.swagger.modelgen.ModelRegister[org.scalatra.swagger.Model]].get($modelName) match {
            case Some(existingModel) =>
              existingModel
            case None =>
              val model =
                org.scalatra.swagger.Model(
                  id = $modelName,
                  name = $modelName,
                  qualifiedName = Some($qualifiedName),
                  properties = List(..$params))
              val registeredModel = implicitly[ar.com.crypticmind.swagger.modelgen.ModelRegister[org.scalatra.swagger.Model]].register(model)
              ..$generateDependentTypes
              registeredModel
          }
        }
        """
      }
    }
  }
}
