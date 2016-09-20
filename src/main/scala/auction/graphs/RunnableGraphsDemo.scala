package auction.graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, Supervision, ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._
import ActorAttributes.supervisionStrategy
import Supervision.resumingDecider
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Try, Random}

/**
 * Created by simun on 20.9.2016..
 */
object RunnableGraphsDemo extends App {

  implicit val system = ActorSystem("graphs")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1 second)

  /**
   * creating graph and getting materialized value from it
   */
  val in = Source(1 to 100)
  val out = Sink.fold[Int, Int](0)(_ + _ )

  val g: RunnableGraph[(NotUsed, Future[Int])] = RunnableGraph.fromGraph(GraphDSL.create(in, out)((_, _)) { implicit  b => (in, out) =>
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Int](2))
    val merge = b.add(Merge[Int](2))

    val f1, f2, f3, f4 = Flow[Int].map(_ * 1)

    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
    bcast ~> f4 ~> merge

    ClosedShape
  })

  val (notUsed, sumFromFuture): (NotUsed, Future[Int]) =  g run()

  lazy val osum = (1 to 100) map (_*1) sum
  lazy val expectedValue =  osum * 2

  sumFromFuture map { result =>
    println(expectedValue == result)
  }

  /**
   * combining sources
   */
  val source1 = Source(List(1,2, 3))
  val source2 = Source(List(1,2, 3))

  val merged = Source.combine(source1, source2)(Merge(_))
  merged.runWith(out) foreach println

  /**
   * Exception handling ''resumingDecider'' for supervisorStrategy is used in order to drop Failure and continue with processing
   */

  def futureOperation(): Future[Int] = Future {
    val random = Random.nextInt(1000)
    random %2 == 0 match {
      case true => random
      case false => throw new ArithmeticException("some error")
    }
  }

  val futureIntFlow = Source(1 to 100).via(Flow[Int].mapAsync(4) { value => futureOperation() }).withAttributes(supervisionStrategy(resumingDecider))
  val futureResult: Future[Int] = futureIntFlow runWith(out)
  futureResult map { sum =>
    println(sum)
    sum
  } andThen { case sum: Try[Int] => system.terminate() }

  /**
   * close program on finish
   */
  Await.result(futureResult, timeout.duration)
}
