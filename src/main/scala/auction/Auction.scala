package auction

import akka.actor.{Actor, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import auction.Auction.{GetBids, Bids, Bid}
import spray.json._

import scala.util.Random

/**
 * Created by simun on 17.8.2016..
 */
class Auction extends Actor {
  private var bidList = List.empty[Bid]

  override def receive: Receive = {
    case b @ Bid(bid, user) => bidList = bidList :+ b
    case bids @ GetBids =>
      if (Random.nextInt(100) % 2 == 0) Thread.sleep(6000)
      sender() ! new Bids(bidList)
  }
}

object Auction {
  case class Bid(userId: String, bid: Int)
  case object GetBids
  case class Bids(bids: List[Bid])

  def props = Props(new Auction)
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val bidJsonFormat = jsonFormat2(Bid)
  implicit val bidsJsonFormat = jsonFormat1(Bids)
}