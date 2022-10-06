package com.tauro.creditcards.model

import com.tauro.creditcards.model.CreditCardProtocol.{CreditCard, CsCardResponse, ScoredCardsResponse}

object ResponseTransformer {

  trait ResponseTransformer[T] {
    def transformResponse(e: T): CreditCard
  }

  implicit class TransformerEnrichment[T](value: T) {
    def toCreditCard(implicit transformer: ResponseTransformer[T]): CreditCard = transformer.transformResponse(value)
  }

  implicit object CsCardsRequestTransformer extends ResponseTransformer[CsCardResponse] {
    override def transformResponse(r: CsCardResponse): CreditCard = {
      val score = BigDecimal(r.eligibility * 10 * Math.pow(1 / r.apr, 2)).setScale(3, BigDecimal.RoundingMode.DOWN)
      CreditCard(provider = "CSCards", name = r.cardName, apr = r.apr, cardScore = score.toDouble)
    }
  }

  implicit object ScoredCardsRequestTransformer extends ResponseTransformer[ScoredCardsResponse] {
    override def transformResponse(r: ScoredCardsResponse): CreditCard = {
      val score = BigDecimal(r.approvalRating * 100 * Math.pow(1 / r.apr, 2)).setScale(3, BigDecimal.RoundingMode.DOWN)
      CreditCard(provider = "ScoredCards", name = r.card, apr = r.apr, cardScore = score.toDouble)
    }
  }

}
