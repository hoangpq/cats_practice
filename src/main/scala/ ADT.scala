import ADT.Proxy.Result
import Api.Answer
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.{Applicative, Functor, MonadError}
import cats.implicits._
import monocle.Iso
sealed trait Pred

object Pred {
  case object Keep extends Pred

  case object Discard extends Pred
}

/*class List[A] {
  def filterBy(p: A => Pred): List[A] = ???
}*/


object Api {
  sealed trait Answer

  object Answer {
    case object Yes extends Answer

    case object No extends Answer
  }

  def make[F[_] : Applicative]: Api[F] =
    new Api[F] {
      def get: F[Answer] = Answer.No.pure[F].widen
    }
}

trait Api[F[_]] {

  import Api.Answer

  def get: F[Answer]
}

object ADT {

  implicit class ListOps[A](xs: List[A]) {
    def filterBy(p: A => Pred): List[A] =
      xs.filter {
        p(_) match {
          case Pred.Keep => true
          case Pred.Discard => false
        }
      }
  }


  // Coming back to the BoolApi[F] previously defined, if we can't change it, we can
  // create own API on top of it. I call it a proxy
  trait Proxy[F[_]] {
    def get: F[Result]
  }

  trait BoolApi[F[_]] {
    def get: F[Boolean]
  }

  // implement instance
  object BoolApi {
    def make[F[_] : Applicative]: BoolApi[F] =
      new BoolApi[F] {
        def get: F[Boolean] = true.pure[F]
      }
  }

  object Proxy {
    sealed trait Result

    object Result {
      case object Yes extends Result

      case object No extends Result

      val _Bool: Iso[Boolean, Result] =
        Iso[Boolean, Result](if (_) Yes else No) {
          case Yes => true
          case No => false
        }
    }

    def make[F[_] : Functor](boolApi: BoolApi[F]): Proxy[F] =
      new Proxy[F] {
        /*def get: F[Result] = boolApi.get.map(
          if (_) Result.Yes else Result.No
        )*/

        // At last, we can refactor the implementation of Proxy this way
        def get: F[Result] = boolApi.get.map(Result._Bool.get)
      }

  }

  val boolApi = BoolApi.make[IO]
  val api = Api.make[IO]
  val proxy = Proxy.make(boolApi)

  def main(args: Array[String]): Unit = {

    val p2 = api.get.flatMap {
      case Answer.Yes => IO.println("YES!")
      case Answer.No => IO.println("NO!")
    }
    p2.unsafeRunSync()

    val p3 = proxy.get.flatMap {
      case Result.Yes => IO.println("YES!")
      case Result.No => IO.println("NO!")
    }
    p3.unsafeRunSync()

    val res = List
      .range(1, 11)
      .filterBy { n =>
        if (n > 5) Pred.Keep else Pred.Discard
      }
    println(res)
  }
}