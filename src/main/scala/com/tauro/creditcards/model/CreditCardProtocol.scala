package com.tauro.creditcards.model

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

object CreditCardProtocol {

  final case class CreditCardRequest(name: String, creditScore: Int, salary: Int)

  final case class CsCardRequest(name: String, creditScore: Int)

  final case class ScoredCardsRequest(name: String, score: Int, salary: Int)

  final case class CreditCard(provider: String, name: String, apr: Double, cardScore: Double)

  implicit final val creditCardRequestDecoder: Decoder[CreditCardRequest] = deriveDecoder[CreditCardRequest].emap(creditCardRequest =>
    if ((creditCardRequest.creditScore >= 0 && creditCardRequest.creditScore <= 700 && creditCardRequest.salary >= 0)) Right(creditCardRequest)
    else Left("credit score is out of range")
  )

  implicit final val csCardRequestEncoder: Encoder[CsCardRequest] = deriveEncoder
  implicit final val scoredCardsRequestEncoder: Encoder[ScoredCardsRequest] = deriveEncoder
  implicit final val creditCardEncoder: Encoder[CreditCard] = deriveEncoder
}
