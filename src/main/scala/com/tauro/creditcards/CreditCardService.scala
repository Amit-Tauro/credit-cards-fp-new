package com.tauro.creditcards

import cats.effect.kernel.Concurrent
import com.tauro.creditcards.integration.{CsCards, GatewayError, ScoredCards}
import com.tauro.creditcards.model.CreditCardProtocol._
import cats.implicits._
import cats.effect.implicits._
import com.tauro.creditcards.model.GatewayProtocol._

final case class CreditCardApiError(msg: String) extends RuntimeException

trait CreditCardService[F[_]] {
  def fetchCards(req: CreditCardRequest): F[List[CreditCard]]
}

class CreditCardServiceImpl[F[_]: Concurrent](csCards: CsCards[F],
                                        scoredCards: ScoredCards[F],
                                        transformerService: TransformerService) extends CreditCardService[F] {

  override def fetchCards(req: CreditCardRequest): F[List[CreditCard]] = {
    (csCards.fetch(req), scoredCards.fetch(req)).parMapN {
      (csCardsResp, scoredCardsResp) => sortCreditCards(csCardsResp, scoredCardsResp)
    }
  }

  private def sortCreditCards(csCards: Either[GatewayError, List[GatewayResponse]], scoredCards: Either[GatewayError, List[GatewayResponse]]): List[CreditCard] = {
    if (csCards.isLeft && scoredCards.isLeft) throw CreditCardApiError("All credit card providers are down")
    else merge(csCards, scoredCards).sortWith(sortingScore)
  }

  private def merge(csCards: Either[GatewayError, List[GatewayResponse]], scoredCards: Either[GatewayError, List[GatewayResponse]]): List[CreditCard] =
    transformerService.normalise(csCards.getOrElse(List.empty)) ::: transformerService.normalise(scoredCards.getOrElse(List.empty))

  private def sortingScore(cs1: CreditCard, cs2: CreditCard): Boolean = {
    cs1.cardScore > cs2.cardScore
  }
}
