package ar.com.crypticmind.swagger.modelgen

import com.wordnik.swagger.model.Model

trait ModelRegister {
  def get(id: String): Option[Model]
  def register(model: Model): Model
}
