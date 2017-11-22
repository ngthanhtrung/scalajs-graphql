// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import scala.util.Try

import sangria.ast
import sangria.validation.TypeInfo

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

private[codegen] final class SchemaTraversal(
  schema: Schema[_, _]
) {

  private[this] val typeInfo = new TypeInfo(schema)

  def scope[A](node: ast.AstNode)(action: => Result[A]): Result[A] = {
    for {
      _ <- Right(typeInfo.enter(node))
      attempt <- action.attempt
      _ = typeInfo.leave(node)
      result <- attempt
    } yield result
  }

  def currentNode: Result[ast.AstNode] = {
    typeInfo.ancestors.lastOption.toRight(EmptyNodeStackException)
  }

  def currentType: Result[Type] = {
    for {
      node <- currentNode
      tpe <- typeInfo.tpe.toRight(TypeNotAvailableException(node))
    } yield tpe
  }

  def currentNamedType: Result[Type with Named] = {
    for {
      node <- currentNode
      tpe <- currentType
      namedType <- Try(tpe.namedType).toEither.left.map {
        NamedTypeNotAvailableException(tpe, node, _)
      }
    } yield namedType
  }

  private[this] def specificCurrentType[A](
    filter: (ast.AstNode, Type) => Result[A]
  ): Result[A] = {
    for {
      node <- currentNode
      tpe <- currentType
      specificType <- filter(node, tpe)
    } yield specificType
  }

  def currentCompositeType: Result[CompositeType[_]] = {
    specificCurrentType { (node, tpe) =>
      tpe match {
        case compositeType: CompositeType[_] => Right(compositeType)
        case _ => Left(UnexpectedTypeException(tpe, classOf[CompositeType[_]], node))
      }
    }
  }

  def currentObjectType: Result[ObjectType[_, _]] = {
    specificCurrentType { (node, tpe) =>
      tpe match {
        case objectType: ObjectType[_, _] => Right(objectType)
        case _ => Left(UnexpectedTypeException(tpe, classOf[ObjectType[_, _]], node))
      }
    }
  }
}
