package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.{ Model => WordnikModel }
import org.scalatra.swagger.{ Model => ScalatraModel }

trait WordnikModelRegister {
  def get(id: String): Option[WordnikModel]
  def register(model: WordnikModel): WordnikModel
}

trait ScalatraModelRegister {
  def get(id: String): Option[ScalatraModel]
  def register(model: ScalatraModel): ScalatraModel
}
