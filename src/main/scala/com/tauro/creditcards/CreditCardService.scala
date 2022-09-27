package com.tauro.creditcards

import cats.effect.Sync
import com.tauro.creditcards.CreditCardProtocol._
import cats.implicits._

trait CreditCardService[F[_]] {
  def fetchCards(req: CreditCardRequest): F[List[CreditCard]]
}

class CreditCardServiceImpl[F[_]: Sync](creditCardGateway: CreditCardGateway[F]) extends CreditCardService[F] {

  override def fetchCards(req: CreditCardRequest): F[List[CreditCard]] = {
    creditCardGateway.csCards(req)
    for {
      csCards <- creditCardGateway.csCards(req)
      scoredCards <- creditCardGateway.scoredCards(req)
    } yield creditCards(csCards, scoredCards)
  }

  private def creditCards(csCards: List[CsCardResponse], scoredCards: List[ScoredCardsResponse]): List[CreditCard] = {
    (handleCsCards(csCards) ::: handleScoredCards(scoredCards)).sortWith(sortingScore)
  }

  private def handleCsCards(csCards: List[CsCardResponse]): List[CreditCard] =
    csCards.map(r => CreditCard(provider = "CSCards", name = r.cardName, apr = r.apr, cardScore = csScore(r)))

  private def handleScoredCards(scoredCards: List[ScoredCardsResponse]): List[CreditCard] =
    scoredCards.map(r => CreditCard(provider = "ScoredCards", name = r.card, apr = r.apr, cardScore = scoredCardsScore(r)))

  private def csScore(cs: CsCardResponse): Double = {
    cs.eligibility * (Math.pow(1 / cs.apr, 2))
  }

  private def scoredCardsScore(sc: ScoredCardsResponse): Double = {
    val eligibility: Double = sc.approvalRating*10
    eligibility * (Math.pow(1 / sc.apr, 2))
  }

  private def sortingScore(cs1: CreditCard, cs2: CreditCard): Boolean = {
    cs1.cardScore > cs2.cardScore
  }
}

// todo cats effect having to use both sync and concurrent - what does this mean
// todo what is adaptError and how do we want to handle errors here? What are partial functions
// todo start service with bash script
// todo make it easy to add another partner - generics?
// todo how to run python tests
// todo review tech test critera on notion
