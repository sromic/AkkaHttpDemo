package auction

import akka.actor.{ActorLogging, Props}
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import auction.Auction.{GetBids, Bids, Bid}
import spray.json._

import scala.util.Random

/**
 * Created by simun on 17.8.2016..
 */
final class Auction extends PersistentActor with ActorLogging {
  import Auction._
  import context.dispatcher

  private var bidList = List.empty[Bid]

  override def persistenceId = Name

  override def receiveCommand = {
    case b @ Bid(bid, user) => handleAddBid(b)
    case bids @ GetBids => handleBids()
  }

  override def receiveRecover = {
    case BidPlaced(bid) =>
      bidList = bidList :+ bid
      log.info(s"Bid placed with bid: ${bid}")
  }

  private[this] def handleAddBid(bid: Bid) = {
    persist(BidPlaced(bid))(receiveRecover)
  }

  private[this] def handleBids() = {
    if (Random.nextInt(100) % 2 == 0) Thread.sleep(6000)
    sender() ! new Bids(bidList)
  }
}

object Auction {
  final val Name = "auction"
  final case class Bid(userId: String, bid: Int)
  final case object GetBids
  final case class Bids(bids: List[Bid])

  final case class BidPlaced(b: Bid)

  def props = Props(new Auction)
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val bidJsonFormat = jsonFormat2(Bid)
  implicit val bidsJsonFormat = jsonFormat1(Bids)
}