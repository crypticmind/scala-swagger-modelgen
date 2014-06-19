package ar.com.crypticmind.swagger.modelgen

import org.scalatra.swagger.Model

import scala.language.experimental.macros
import scala.reflect.macros.Context

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

      c.Expr[Model] {

        Apply(
          Select(
            Select(
              Select(
                Ident(
                  newTermName("org")
                ),
                newTermName("scalatra")
              ),
              newTermName("swagger")
            ),
            newTermName("Model")
          ),
          List(
            AssignOrNamedArg(
              Ident(
                newTermName("id")
              ),
              Literal(
                Constant("id")
              )
            ),
            AssignOrNamedArg(
              Ident(
                newTermName("name")
              ),
              Literal(
                Constant("id")
              )
            ),
            AssignOrNamedArg(
              Ident(
                newTermName("qualifiedName")
              ),
              Apply(
                Ident(
                  newTermName("Some")
                ),
                List(
                  Literal(
                    Constant("id")
                  )
                )
              )
            )
          )
        )

      }

      //      val fields = tpe.declarations.collectFirst {
//        case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
//      }.get.paramss.head
//
//      val m = new ScalatraModelPropertyMapping[c.type](c)
//
//      val params = fields.map { field =>
//        val fieldName = field.name.toString
//
//        val fieldMG = m.selectFor(field.typeSignature)
//
//        q"$fieldName -> ${fieldMG.toModelProperty}"
//      }
//
//      val modelName = tpe.typeSymbol.name.toString
//      val qualifiedName = tpe.typeSymbol.asClass.fullName
//
//      val dependentTypes =
//        fields
//          .map(field => m.selectFor(field.typeSignature).dependentTypes)
//          .foldLeft(Set.empty[c.Type])((s1, s2) => s1 ++ s2)
//          .filterNot(dependentType => dependentType =:= tpe)
//          .--(filterDependentTypes)
//
//      val generateDependentTypes = dependentTypes.map(dependentType => processType(dependentType, dependentTypes ++ Set(tpe)))
//
//      c.Expr[Model] {
//        q""" {
//          implicitly[ar.com.crypticmind.swagger.modelgen.ScalatraModelRegister].get($modelName) match {
//            case Some(existingModel) =>
//              existingModel
//            case None =>
//              val model =
//                org.scalatra.swagger.Model(
//                  id = $modelName,
//                  name = $modelName,
//                  qualifiedName = Some($qualifiedName),
//                  properties = List(..$params))
//              val registeredModel = implicitly[ar.com.crypticmind.swagger.modelgen.ScalatraModelRegister].register(model)
//              ..$generateDependentTypes
//              registeredModel
//          }
//        }
//        """
//      }
    }
  }
}
