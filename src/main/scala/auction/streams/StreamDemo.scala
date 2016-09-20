package auction.streams

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by simun on 20.9.2016..
 */
object StreamDemo extends App {

  implicit val system = ActorSystem("Streams")
  implicit val materializer = ActorMaterializer()

  val source = Source(1 to 100)
  source runForeach(i => println(i))

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val result: Future[IOResult] = factorials.map(num => ByteString(s"$num\n"))
    .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  def lineSink(filename: String): Sink[String, Future[IOResult]] = {
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)
  }

  def write2File[T](filename: String): Sink[T, Future[IOResult]] = {
    Flow[T]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)
  }

  factorials.map(_.toString).runWith(lineSink("factorial2.txt"))

  /*val done = factorials
    .zipWith(Source(1 to 100))((num, index) => s"$index! = $num")
    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .runForeach(println)
*/
  final case class Author(handle: String)
  final case class HashTag(name: String)
  final case class Tweet(author: Author, timestamp: Long, body: String) {
    def hashtags: Set[HashTag] = {
      body.split(" ").collect { case t if t.startsWith("#") => HashTag(t) }.toSet
    }
  }

  val akka = HashTag("#2")

  def getTweets = (1 to 100) map ( i => Tweet(Author(s"$i"), i, s"#$i"))
  val tweets: Source[Tweet, NotUsed] = Source.apply(getTweets)

  val authors: Source[Author, NotUsed] = tweets.filter(_.hashtags.contains(akka)).map(_.author)

  authors.runWith(Sink.foreach(println))

  //flattening
  val hashtags: Source[HashTag, NotUsed] = tweets.mapConcat(_.hashtags.toList)

  //GraphDSL and broadcasting
  val writeAuthors: Sink[Author, Future[IOResult]] = write2File[Author]("authors.txt")
  val writeHashTags: Sink[HashTag, Future[IOResult]] = write2File[HashTag]("hashtags.txt")
  
  val g = RunnableGraph.fromGraph(GraphDSL.create() {implicit  b =>
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Tweet](2))
    tweets ~> bcast.in
    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashTags

    ClosedShape
  })

  //g.run()

  //backpressure
  tweets.buffer(10, OverflowStrategy.backpressure).map{ t => Thread.sleep(2000); t}.runWith(Sink.foreach(println))

  //materialized values
/*  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map( _ => 1)
  val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  val counterGraph: RunnableGraph[Future[Int]] = tweets.via(count).toMat(sumSink)(Keep.right)

  val sumCount: Future[Int] = counterGraph.run()

  sumCount foreach println*/



  system.terminate() foreach( println )

}
