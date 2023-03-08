import cats.MonadError
import cats.implicits._

object Errors {

  def divide(a: Int, b: Int): Either[String, Int] = {
    if (b == 0) Left("Cannot divide by zero")
    else Right(a / b)
  }

  def main(args: Array[String]): Unit = {
    val eitherMonadError = MonadError[Either[String, *], String]
  }
}