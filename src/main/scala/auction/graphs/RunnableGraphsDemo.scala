package auction.graphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._

import scala.concurrent.Future

/**
 * Created by simun on 20.9.2016..
 */
object RunnableGraphsDemo extends App {

  implicit val system = ActorSystem("graphs")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

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

  system terminate() foreach println
}
