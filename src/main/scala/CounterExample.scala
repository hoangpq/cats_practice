import cats.effect.std.{Semaphore, Supervisor}
import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits._

import scala.concurrent.duration._

object CounterExample extends IOApp.Simple {
  def incrementCounter(ref: Ref[IO, Int]): IO[Unit] =
    ref.update(_ + 1)

  val program: IO[Unit] = {
    for {
      counterRef <- Ref[IO].of(0)
      _ <- List.fill(100)(incrementCounter(counterRef)).parSequence
      finalValue <- counterRef.get
      _ <- IO.println(s"Final value: $finalValue")
    } yield ()
  }

  val p2: IO[Unit] = for {
    ref1 <- Ref[IO].of(200)

    counterRef <- Ref[IO].of(0)
    _ <- List.fill(100)(incrementCounter(counterRef)).parSequence

    finalVal <- ref1.get combine counterRef.get
    _ <- IO.println(s"Final value: $finalVal")
  } yield ()

  def randomSleep: IO[Unit] =
    IO(scala.util.Random.nextInt(108)).flatMap { ms =>
      IO.sleep((ms + 700).millis)
    }

  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println("Running P1")) >> randomSleep

  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println("Running P2")) >> randomSleep

  def diy(id: Int): IO[Unit] =
    IO.println(s"Child process $id starting...") >>
      IO.sleep(2.second) >>
      IO.println(s"Child process $id finished.")

  /*def run: IO[Unit] =
    Supervisor[IO].use { s =>
      Semaphore[IO](1).flatMap { sem =>
        s.supervise(p1(sem).foreverM).void *>
          s.supervise(p2(sem).foreverM).void *>
          IO.sleep(5.seconds).void
      }
    }*/

  def run: IO[Unit] =
    Supervisor[IO].use { s =>
      Semaphore[IO](1).flatMap { sem =>
        val p1 = s.supervise(diy(1).foreverM).void
        val p2 = s.supervise(diy(2).foreverM).void

        p1.start *> p2.start *> IO.sleep(10.seconds) >>
          IO.println("Shutting down supervisor...")
      }
    }
}

trait Items[F[_]] {
  def getAll: F[Seq[Item]]

  case class Item()
}

/*
class Program[F[_]](items: Items[F[_]]) {
  def calcTotalPrice: F[BigDecimal] =
    items.getAll.map { seq  =>
      seq.toList
        .map(_.price)
        .foldLeft(0)((acc, p)  => acc + p)
    }
}
 */

// How to we know it is safe to call toList? What is the Items interpreter
// users a Stream (or LazyList since Scala 2.13.0) representing possibly
// infinite items?

// To be safe, prefer to use more specific datatype such as List, Vector, Chain,
// or fs2.Stream, depending on your specific goals and desired performance characteristics


object SupervisorExample extends IOApp.Simple {
  // global access
  // lazy val sem: Semaphore[IO] = Semaphore[IO](1).unsafeRunSync()

  def childProcess(id: Int, sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println(s"$id running...")) >>
      IO.sleep(700.millis)

  def run(): IO[Unit] =
    Supervisor[IO].use { s =>
      Semaphore[IO](1).flatMap { sem =>
        val p1 = s.supervise(childProcess(1, sem).foreverM).void
        val p2 = s.supervise(childProcess(2, sem).foreverM).void

        p1 *> p2 *> IO.sleep(5.second).void >> IO.println("Existed...")
      }
    }
}