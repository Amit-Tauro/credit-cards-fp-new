package com.tauro.creditcards.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object GatewayProtocol {

  sealed trait GatewayResponse

  final case class CsCardResponse(cardName: String, apr: Double, eligibility: Double) extends GatewayResponse

  final case class ScoredCardsResponse(card: String, apr: Double, approvalRating: Double) extends GatewayResponse

  implicit final val scoredCardsResponseDecoder: Decoder[List[ScoredCardsResponse]] = deriveDecoder
  implicit final val csCardResponseDecoder: Decoder[List[CsCardResponse]] = deriveDecoder
}
