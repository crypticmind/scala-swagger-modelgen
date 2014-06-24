package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.ModelProperty
import scala.reflect.macros.Context

class WordnikModelPropertyMapping[C <: Context](val c: C) {
  import c.universe._

  val typeHelper = new TypeHelper[c.type](c)

  abstract class ModelPropertyGenerator {
    def toModelProperty: c.Expr[ModelProperty]
    def dependentTypes: Set[c.Type]
  }

  object StringModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "StringModelPropertyGenerator"
    val toModelProperty =
      c.Expr[ModelProperty] {
        Apply(
          Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelProperty")),
          List(
            AssignOrNamedArg(Ident(newTermName("type")), Literal(Constant("string"))),
            AssignOrNamedArg(Ident(newTermName("qualifiedType")), Literal(Constant("java.lang.String"))),
            AssignOrNamedArg(Ident(newTermName("required")), Literal(Constant(true)))))
      }
    val dependentTypes = Set.empty[c.Type]
  }

  object IntModelPropertyGenerator extends ModelPropertyGenerator {
    override val toString = "IntModelPropertyGenerator"
    val toModelProperty =
      c.Expr[ModelProperty] {
        Apply(
          Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelProperty")),
          List(
            AssignOrNamedArg(Ident(newTermName("type")), Literal(Constant("int"))),
            AssignOrNamedArg(Ident(newTermName("qualifiedType")), Literal(Constant("scala.Int"))),
            AssignOrNamedArg(Ident(newTermName("required")), Literal(Constant(true)))))
      }
    val dependentTypes = Set.empty[c.Type]
  }

  class ObjectModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectName = t.typeSymbol.name.toString
    val qualifiedType = t.typeSymbol.asClass.fullName
    override val toString = "ObjectModelPropertyGenerator"
    val toModelProperty =
      c.Expr[ModelProperty] {
      Apply(
         Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelProperty")),
         List(
           AssignOrNamedArg(Ident(newTermName("type")), Literal(Constant(objectName))),
           AssignOrNamedArg(Ident(newTermName("qualifiedType")), Literal(Constant(qualifiedType))),
           AssignOrNamedArg(Ident(newTermName("required")), Literal(Constant(true))),
           AssignOrNamedArg(Ident(newTermName("items")), Apply(Ident(newTermName("Some")),
             List(
               Apply(Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelRef")),
                 List(
                   AssignOrNamedArg(Ident(newTermName("type")), Literal(Constant(objectName))))))))))
      }
    val dependentTypes = Set(t)
  }

  class OptionModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectType = t.asInstanceOf[TypeRefApi].args.head.typeSymbol.asClass.fullName
    val mapperForType = selectFor(t.asInstanceOf[TypeRefApi].args.head)
    override val toString = s"OptionModelPropertyGenerator(${mapperForType.toString})"
    val toModelProperty =
      c.Expr[ModelProperty] {
        Apply(
          Select(mapperForType.toModelProperty.tree, newTermName("copy")),
          List(
            AssignOrNamedArg(Ident(newTermName("required")), Literal(Constant(false)))))
      }
    val dependentTypes = mapperForType.dependentTypes
  }

  class EnumModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    import c.universe.Flag._
    override val toString = s"EnumModelPropertyGenerator(${t.typeSymbol.owner})"
    val typeName = typeHelper.tree(typeHelper.getPath(t).reverse)
    val toModelProperty =
      c.Expr[ModelProperty] {
        Block(
          List(
            Import(
              Select(Select(Ident(newTermName("reflect")), newTermName("runtime")), newTermName("universe")),
              List(ImportSelector(nme.WILDCARD, 60, null, -1))),
            Import(Select(Ident(newTermName("reflect")), newTermName("runtime")),
              List(ImportSelector(nme.WILDCARD, 96, null, -1))),
            ValDef(
              Modifiers(), newTermName("tpe"), TypeTree(),
              TypeApply(
                Select(TypeApply(Ident(newTermName("typeOf")), List(typeName)), newTermName("asInstanceOf")),
                List(Ident(newTypeName("TypeRef"))))),
            ValDef(
              Modifiers(), newTermName("pre"), TypeTree(),
              Select(Ident(newTermName("tpe")), newTermName("pre"))),
            ValDef(
              Modifiers(), newTermName("mod"), TypeTree(),
              Select(Select(Ident(newTermName("pre")), newTermName("termSymbol")), newTermName("asModule"))),
            ValDef(
              Modifiers(), newTermName("modMirror"), TypeTree(),
              Apply(
                Select(Ident(newTermName("currentMirror")), newTermName("reflectModule")), List(Ident(newTermName("mod"))))),
            ValDef(
              Modifiers(), newTermName("modInst"), TypeTree(),
              Select(Ident(newTermName("modMirror")), newTermName("instance"))),
            ValDef(
              Modifiers(), newTermName("values"), TypeTree(),
              Apply(
                Select(Select(Select(TypeApply(Select(Ident(newTermName("modInst")), newTermName("asInstanceOf")), List(Ident(newTypeName("Enumeration")))), newTermName("values")), newTermName("toList")), newTermName("map")),
                List(
                  Function(List(ValDef(Modifiers(PARAM), newTermName("x$1"), TypeTree(), EmptyTree)), Select(Ident(newTermName("x$1")), newTermName("toString"))))))),
          Apply(
            Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelProperty")),
            List(
              AssignOrNamedArg(Ident(newTermName("type")), Literal(Constant("string"))),
              AssignOrNamedArg(Ident(newTermName("qualifiedType")), Literal(Constant("java.lang.String"))),
              AssignOrNamedArg(Ident(newTermName("required")), Literal(Constant(true))),
              AssignOrNamedArg(Ident(newTermName("allowableValues")),
                Apply(
                  Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("AllowableListValues")),
                  List(Ident(newTermName("values"))))))))
      }
    val dependentTypes = Set.empty[c.Type]
   }

  class IterModelPropertyGenerator(t: c.Type) extends ModelPropertyGenerator {
    val objectType = t.asInstanceOf[TypeRefApi].args.head.typeSymbol.asClass.fullName
    val mapperForType = selectFor(t.asInstanceOf[TypeRefApi].args.head)
    override val toString = s"IterModelPropertyGenerator(${mapperForType.toString})"
    val toModelProperty =
      c.Expr[ModelProperty] {
        Block(
          List(
            ValDef(Modifiers(), newTermName("refType"), TypeTree(),
              Select(mapperForType.toModelProperty.tree, newTermName("type")))),
          Apply(Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelProperty")),
            List(
              AssignOrNamedArg(Ident(newTermName("type")), Literal(Constant("array"))),
              AssignOrNamedArg(Ident(newTermName("qualifiedType")), Literal(Constant("scala.collection.Iterable"))),
              AssignOrNamedArg(Ident(newTermName("required")), Literal(Constant(true))),
              AssignOrNamedArg(Ident(newTermName("items")),
                Apply(Ident(newTermName("Some")), List(
                  Apply(Select(Select(Select(Select(Ident(newTermName("com")), newTermName("wordnik")), newTermName("swagger")), newTermName("model")), newTermName("ModelRef")),
                    List(AssignOrNamedArg(Ident(newTermName("type")), Ident(newTermName("refType")))))))))))
      }
    val dependentTypes = mapperForType.dependentTypes
  }

   def selectFor(t: c.Type): ModelPropertyGenerator = t match {
     case str if t <:< c.typeOf[String] => StringModelPropertyGenerator
     case int if t <:< c.typeOf[Int] => IntModelPropertyGenerator
     case opt if t <:< c.typeOf[Option[_]] => new OptionModelPropertyGenerator(opt)
     case enm if t <:< c.typeOf[Enumeration#Value] => new EnumModelPropertyGenerator(enm)
     case itr if t <:< c.typeOf[Iterable[_]] => new IterModelPropertyGenerator(itr)
     case other => new ObjectModelPropertyGenerator(other)
   }

}
