package com.tauro.creditcards

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


object CreditCardProtocol {

  final case class CreditCardRequest(name: String, creditScore: Int, salary: Int)
  final case class CsCardRequest(name: String, creditScore: Int)
  final case class CsCardResponse(cardName: String, apr: Double, eligibility: Double)
  final case class ScoredCardsRequest(name: String, score: Int, salary: Int)
  final case class ScoredCardsResponse(card: String, apr: Double, approvalRating: Double)
  final case class CreditCard(provider: String, name: String, apr: Double, cardScore: Double)

  implicit final val creditCardRequestDecoder: Decoder[CreditCardRequest] = deriveDecoder
  implicit final val scoredCardsResponseDecoder: Decoder[ScoredCardsResponse] = deriveDecoder

  implicit final val csCardRequestEncoder: Encoder[CsCardRequest] = deriveEncoder
  implicit final val csCardResponseDecoder: Decoder[CsCardResponse] = deriveDecoder
  implicit final val scoredCardsRequestEncoder: Encoder[ScoredCardsRequest] = deriveEncoder
  implicit final val creditCardEncoder: Encoder[CreditCard] = deriveEncoder
}
