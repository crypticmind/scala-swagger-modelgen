package ar.com.crypticmind.swagger.modelgen

trait ModelRegister[T] {
  def get(id: String): Option[T]
  def register(model: T): T
  def names: Set[String]
}
