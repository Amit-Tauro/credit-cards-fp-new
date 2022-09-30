package com.tauro.creditcards

import cats.effect.Sync
import com.tauro.creditcards.CreditCardProtocol._
import cats.implicits._

final case class GatewayError(msg: String) extends RuntimeException

trait CreditCardService[F[_]] {
  def fetchCards(req: CreditCardRequest): F[List[CreditCard]]
}

class CreditCardServiceImpl[F[_]: Sync](creditCardGateway: CreditCardGateway[F]) extends CreditCardService[F] {

  override def fetchCards(req: CreditCardRequest): F[List[CreditCard]] = {
    for {
      csCards <- creditCardGateway.csCards(req)
      scoredCards <- creditCardGateway.scoredCards(req)
    } yield sortCreditCards(csCards, scoredCards)
  }

  private def sortCreditCards(csCards: Either[CsCardError, List[CsCardResponse]], scoredCards: Either[ScoredCardError, List[ScoredCardsResponse]]): List[CreditCard] = {
    if (csCards.isLeft && scoredCards.isLeft) throw GatewayError("All credit card providers are down")
    else (handleCsCards(csCards.getOrElse(List.empty)) ::: handleScoredCards(scoredCards.getOrElse(List.empty))).sortWith(sortingScore)
  }

  private def handleCsCards(csCards: List[CsCardResponse]): List[CreditCard] =
    csCards.map(r => CreditCard(provider = "CSCards", name = r.cardName, apr = r.apr, cardScore = csScore(r)))

  private def handleScoredCards(scoredCards: List[ScoredCardsResponse]): List[CreditCard] =
    scoredCards.map(r => CreditCard(provider = "ScoredCards", name = r.card, apr = r.apr, cardScore = scoredCardsScore(r)))

  private def csScore(cs: CsCardResponse): Double = {
    val num = cs.eligibility * Math.pow((1/cs.apr), 2)*10
    Math.floor(num * 1000.0) / 1000.0
  }

  private def scoredCardsScore(sc: ScoredCardsResponse): Double = {
    val eligibility: Double = sc.approvalRating*10
    val num = eligibility * Math.pow((1/sc.apr), 2)*10
    Math.floor(num * 1000.0) / 1000.0
  }

  private def sortingScore(cs1: CreditCard, cs2: CreditCard): Boolean = {
    cs1.cardScore > cs2.cardScore
  }
}

// todo cats effect having to use both sync and concurrent - what does this mean
// todo what is adaptError and how do we want to handle errors here? What are partial functions - done
// todo start service with bash script
// todo make it easy to add another partner - generics?
// todo how to run python tests - done
// todo review tech test critera on notion
// todo return error if both apis empty? - done
// todo case _: InvalidMessageBodyFailure => BadRequest() why cant you just use InvalidMessageBodyFailure - done
// todo what does blocking mean? How to make api requests parallel/ concurrent in functional?
// todo why does repsonseLogger need Async and why does implicit circe need Concurrent?
// todo having to recompile every time
