import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.impl.util.JavaAccessors
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{JsonEntityStreamingSupport, EntityStreamingSupport}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import auction.Auction.{Bid, Bids, GetBids}
import auction.{Auction, JsonSupport}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Random


/**
 * Created by simun on 17.8.2016..
 */

object Boot extends App with JsonSupport with GlobalExceptionHandler {

  implicit val system = ActorSystem("AkkaHttp")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // Note that the default support renders the Source as JSON Array
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json().withParallelMarshalling(parallelism = 8, unordered = false)

  val auction = system.actorOf(Auction.props, "auction")

  lazy val getBidsList = (1 to 100000) map (i => Bid(s"$i", i)) toList

  val numbers = Source.fromIterator(() =>
    Iterator.continually(Random.nextInt()))

  val file = new File("C:\\tmp\\logit.txt")

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello world from Akka HTTP</h1>"))
      }
    } ~
      path("random") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`text/plain(UTF-8)`,
              numbers.map(n => ByteString(n))
            )
          )
        }
      } ~
      pathPrefix("resources") {
        pathEndOrSingleSlash {
          getFromBrowseableDirectory(s"C:\\tmp\\")
        } ~
          path("spark" /) {
            getFromBrowseableDirectories(s"C:\\tmp\\spark")
          }
      } ~
      path("bid") {
        post {
          entity(as[Bid]) { bid =>
            auction ! bid
            complete(StatusCodes.Accepted, s"Bid $bid placed")
          }
        } ~
        get {
          implicit val timeout: Timeout = 5.seconds

          val bids: Future[Bids] = (auction ? GetBids)(timeout).mapTo[Bids]

          complete(bids)
        }
      } ~
      path("exception") {
        get {
          throw new RuntimeException("some error")
        }
      } ~
      path("stream") {
        get {
            getFromFile(file)
        }
      } ~
      path("bids") {
        val bids: Source[Bid, NotUsed] = Source(getBidsList)
        complete(bids)
      } ~
      path("obids") {
        val bidList = getBidsList
        val bids = Bids(bidList)

        complete(bids)
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 9080)

  StdIn.readLine() // let it run until user presses return

  bindingFuture flatMap(_.unbind()) onComplete(_ => system.terminate())

}
