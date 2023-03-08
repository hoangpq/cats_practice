import cats.Semigroup
import cats.effect.{IO, IOApp}
import io.circe.Encoder
import monocle.Iso
import cats.instances.try_._

import scala.util.control.NoStackTrace

sealed trait Status

object Status {
  case object Okay extends Status

  case object Unreachable extends Status

  val _Bool: Iso[Status, Boolean] =
    Iso[Status, Boolean] {
      case Okay => true
      case Unreachable => false
    }(if (_) Okay else Unreachable)

  implicit val jsonEncoder: Encoder[Status] =
    Encoder.forProduct1("status")(_.toString)

}

import cats.MonadThrow
import cats.data.NonEmptyList

// import cats.implicits.catsStdInstancesForEither;

case object EmptyCartError extends NoStackTrace

case class Checkout[F[_] : MonadThrow, A](cart: List[A]) {
  private def ensureNonEmpty(xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  import cats.syntax.all._

  def process = for {
    // ensure non empty list, will throw error if empty
    a <- ensureNonEmpty(cart)
    // do some other thing
    b <- a.pure[F]
  } yield b

}

object Errors extends IOApp.Simple {
  def run = {
    val cart = Checkout(List(1, 2, 3))
    cart.process.toEither match {
      case Left(EmptyCartError) => IO.println("Empty cart")
      case Right(_) => IO.println("Not empty")
    }
  }

}
