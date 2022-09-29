package com.tauro.creditcards.service

import cats.effect.Concurrent
import cats.implicits._
import com.tauro.creditcards.model.CreditCardProtocol.{CreditCard, CreditCardRequest, CsCardResponse, ScoredCardsResponse}
import com.tauro.creditcards.service.gateway.{CsCardsGateway, ScoredCardsGateway}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.Client

trait CreditCardService[F[_]] {
  def fetchCards(req: CreditCardRequest): F[List[CreditCard]]
}

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
    } yield (handleCsCards(csCards) ++ handleScoredCards(scoredCards)).sortWith(sortingScore)
  }

  private def handleCsCards(csCards: List[CsCardResponse]): List[CreditCard] =
    csCards.map(r => CreditCard(provider = "CSCards", name = r.cardName, apr = r.apr, cardScore = csScore(r)))

  private def handleScoredCards(scoredCards: List[ScoredCardsResponse]): List[CreditCard] =
    scoredCards.map(r => CreditCard(provider = "ScoredCards", name = r.card, apr = r.apr, cardScore = scoredCardsScore(r)))

  private def csScore(cs: CsCardResponse): Double = cs.eligibility * Math.pow(1 / cs.apr, 2)

  private def scoredCardsScore(sc: ScoredCardsResponse): Double = {
    val eligibility: Double = sc.approvalRating * 10
    eligibility * Math.pow(1 / sc.apr, 2)
  }

  private def sortingScore(cs1: CreditCard, cs2: CreditCard): Boolean = cs1.cardScore > cs2.cardScore
}