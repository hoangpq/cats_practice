import cats.data.EitherNel
import cats.implicits.{catsSyntaxTuple2Parallel, catsSyntaxTuple3Parallel}
import cats.syntax.either._
import eu.timepit.refined.api.{Refined, RefinedTypeOps, Validate}
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.types.string.NonEmptyString
// import cats.implicits._

import Types._

object Main {
  type GTFive = Int Refined Greater[5]

  object GTFive extends RefinedTypeOps[GTFive, Int]

  // composing
  case class MyType(a: NonEmptyString, b: GTFive)

  /*def validate(a: String, b: Int): EitherNel[String, MyType] =
    (NonEmptyString.from(a).toEitherNel, GTFive.from(b).toEitherNel).parMapN(MyType.apply)*/

  case class Person(username: UserName, name: Name, email: Email)

  def mkPerson(u: String, n: String, e: String): EitherNel[String, Person] =
    (
      UserNameR.from(u).toEitherNel.map(UserName.apply),
      NameR.from(n).toEitherNel.map(Name.apply),
      EmailR.from(e).toEitherNel.map(Email.apply)
    ).parMapN(Person.apply)

  import NewtypeRefinedOps._

  def mkPerson2(u: String, n: String, e: String): EitherNel[String, Person] =
    (
      validate[UserName](u),
      validate[Name](n),
      validate[Email](e)
    ).parMapN(Person.apply)

  /*def mkPerson3(u: String, n: String, e: String): EitherNel[String, Person] =
    (
      u.as[UserName].validate,
      n.as[Name].validate,
      e.as[Email].validate
    ).mapN(Person.apply)*/

  def main(args: Array[String]): Unit = {
    val number: Int = 1
    val resOfNum: Either[String, GTFive] = GTFive.from(number)
    // println(resOfNum)
    // val str: String = "some runtime value"
    // val res: Either[String, NonEmptyString] = NonEmptyString.from(str)
    // evaluating this function
    // val resOfMyType = validate("", 3)
    // println(resOfMyType)

    println(mkPerson2("vampire", "Vampire", "vampire@gmail.com"))

  }
}

trait Counter[F[_]] {
  def incr: F[Unit]
  def get: F[Int]
}

import cats.Functor
import cats.effect.kernel.Ref
import cats.syntax.functor._

object Counter {
  def make[F[_]: Functor: Ref.Make]: F[Counter[F]] =
    Ref.of[F, Int](0).map {
      ref => new Counter[F] {
        def incr: F[Unit] = ref.update(_ + 1)
        def get: F[Int] = ref.get
      }
    }
}
object NewtypeRefinedOps {

  import io.estatico.newtype.Coercible
  import io.estatico.newtype.ops._
  import eu.timepit.refined.refineV

  final class NewtypeRefinedPartiallyApplied[A] {
    def apply[T, P](raw: T)(
      implicit c: Coercible[Refined[T, P], A], v: Validate[T, P]
    ): EitherNel[String, A] =
      refineV[P](raw).toEitherNel.map(_.coerce[A])
  }

  def validate[A]: NewtypeRefinedPartiallyApplied[A] = new NewtypeRefinedPartiallyApplied[A]
}