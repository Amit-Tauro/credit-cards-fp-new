package com.tauro.creditcards

import com.tauro.creditcards.model.GatewayProtocol._
import com.tauro.creditcards.model.CreditCardProtocol._

trait TransformerService {
  def normalise(resp: List[GatewayResponse]): List[CreditCard]
}

class TransformerServiceImpl extends TransformerService {

  override def normalise(resp: List[GatewayResponse]): List[CreditCard] = {
    resp.map {
      case resp@CsCardResponse(cardName, apr, _) =>
        CreditCard(provider = "CSCards", name = cardName, apr = apr, cardScore = normaliseScore(resp))
      case resp@ScoredCardsResponse(card, apr, _) =>
        CreditCard(provider = "ScoredCards", name = card, apr = apr, cardScore = normaliseScore(resp))
    }
  }

  private def normaliseScore(resp: GatewayResponse): Double = {
    resp match {
      case CsCardResponse(_, apr, eligibility) =>
        val num = eligibility * Math.pow((1/apr), 2)*10
        Math.floor(num * 1000.0) / 1000.0
      case ScoredCardsResponse(_, apr, approvalRating) =>
        val num = (approvalRating*10) * Math.pow((1/apr), 2)*10
        Math.floor(num * 1000.0) / 1000.0
    }
  }
}


