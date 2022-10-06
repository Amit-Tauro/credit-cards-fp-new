package com.tauro.creditcards.service

import cats.effect.Concurrent
import cats.implicits._
import com.tauro.creditcards.model.CreditCardProtocol.{CreditCard, CreditCardRequest}
import com.tauro.creditcards.model.ResponseTransformer._
import com.tauro.creditcards.service.gateway.{CsCardsGateway, ScoredCardsGateway}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.Client

trait CreditCardService[F[_]] {
  def fetchCards(req: CreditCardRequest): F[List[CreditCard]]
}

// PureConfig for config
// Cats for parallel calls -> traverse
class CreditCardServiceImpl[F[_] : Concurrent](client: Client[F]) extends CreditCardService[F] {

  override def fetchCards(req: CreditCardRequest): F[List[CreditCard]] = {
    //    val csCards = creditCardGateway.csCards(req)
    //      .map {
    //        _.fold(e => {
    //          println(s"Returning empty list for CsCards due to error: $e")
    //          List.empty
    //        }, identity)
    //      }
    //    val scoredCards = creditCardGateway.scoredCards(req)
    //      .map {
    //        _.fold(e => {
    //          println(s"Returning empty list for ScoredCards due to error: $e")
    //          List.empty
    //        }, identity)
    //      }
    //
    //    for {
    //      cs <- csCards
    //      s <- scoredCards
    //    } yield (handleCsCards(cs) ++ handleScoredCards(s)).sortWith(sortingScore)
    for {
      csCards <- new CsCardsGateway(client).fetchCards(req)
      scoredCards <- new ScoredCardsGateway(client).fetchCards(req)
    } yield (csCards.map(_.toCreditCard) ++ scoredCards.map(_.toCreditCard)).sortWith(sortingScore)
  }

  private def sortingScore(cs1: CreditCard, cs2: CreditCard): Boolean = cs1.cardScore > cs2.cardScore
}