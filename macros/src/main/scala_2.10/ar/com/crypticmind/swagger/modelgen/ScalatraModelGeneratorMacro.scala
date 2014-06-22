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

      val fields = tpe.declarations.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
      }.get.paramss.head

      val m = new ScalatraModelPropertyMapping[c.type](c)
      val params = fields.map { field =>
        val fieldName = field.name.toString

        val fieldMG = m.selectFor(field.typeSignature)
        val modelProperty = fieldMG.toModelProperty

        Apply(Select(Literal(Constant(fieldName)), newTermName("$minus$greater")), List(modelProperty.tree))
      }

      val dependentTypes =
        fields
          .map(field => m.selectFor(field.typeSignature).dependentTypes)
          .foldLeft(Set.empty[c.Type])((s1, s2) => s1 ++ s2)
          .filterNot(dependentType => dependentType =:= tpe)
          .--(filterDependentTypes)

      val generateDependentTypes = dependentTypes.map(dependentType => processType(dependentType, dependentTypes ++ Set(tpe)).tree)

      val modelName = tpe.typeSymbol.name.toString
      val qualifiedName = tpe.typeSymbol.asClass.fullName

      c.Expr[Model] {
        Match(
          Apply(
            Select(TypeApply(Ident(newTermName("implicitly")),
              List(Select(Select(Select(Select(Select(Ident(newTermName("ar")), newTermName("com")), newTermName("crypticmind")), newTermName("swagger")), newTermName("modelgen")), newTypeName("ScalatraModelRegister")))), newTermName("get")),
            List(Literal(Constant(modelName)))),
          List(
            CaseDef(Apply(Ident(newTermName("Some")), List(Bind(newTermName("existingModel"), Ident(nme.WILDCARD)))), EmptyTree,
              Ident(newTermName("existingModel"))),
            CaseDef(Ident(newTermName("None")), EmptyTree,
              Block(
                List(
                  ValDef(Modifiers(), newTermName("model"), TypeTree(),
                    Apply(Select(Select(Select(Ident(newTermName("org")), newTermName("scalatra")), newTermName("swagger")), newTermName("Model")),
                      List(
                        AssignOrNamedArg(Ident(newTermName("id")), Literal(Constant(modelName))),
                        AssignOrNamedArg(Ident(newTermName("name")), Literal(Constant(modelName))),
                        AssignOrNamedArg(Ident(newTermName("qualifiedName")), Apply(Ident(newTermName("Some")), List(Literal(Constant(qualifiedName))))),
                        AssignOrNamedArg(Ident(newTermName("properties")), Apply(Ident(newTermName("List")), params))
                      ))),
                  ValDef(Modifiers(), newTermName("registeredModel"), TypeTree(),
                    Apply(Select(TypeApply(Ident(newTermName("implicitly")), List(Select(Select(Select(Select(Select(Ident(newTermName("ar")), newTermName("com")), newTermName("crypticmind")), newTermName("swagger")), newTermName("modelgen")), newTypeName("ScalatraModelRegister")))), newTermName("register")), List(Ident(newTermName("model")))))
                ) ++ generateDependentTypes,
                Ident(newTermName("registeredModel"))))))
      }
    }
  }
}
