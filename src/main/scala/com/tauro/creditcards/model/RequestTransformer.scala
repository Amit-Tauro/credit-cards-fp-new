package com.tauro.creditcards.model

import com.tauro.creditcards.model.CreditCardProtocol.{CreditCardRequest, CsCardRequest, ScoredCardsRequest}

object RequestTransformer {

  trait RequestTransformer[T] {
    def transformRequest(e: CreditCardRequest): T
  }

  implicit class TransformerEnrichment[T](value: CreditCardRequest) {
    def transform(implicit transformer: RequestTransformer[T]): T = transformer.transformRequest(value)
  }

  implicit object CsCardsRequestTransformer extends RequestTransformer[CsCardRequest] {
    override def transformRequest(e: CreditCardRequest): CsCardRequest = CsCardRequest(e.name, e.creditScore)
  }

  implicit object ScoredCardsRequestTransformer extends RequestTransformer[ScoredCardsRequest] {
    override def transformRequest(e: CreditCardRequest): ScoredCardsRequest = ScoredCardsRequest(e.name, e.creditScore, e.salary)
  }






}
