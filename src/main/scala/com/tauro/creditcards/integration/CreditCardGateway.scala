package com.tauro.creditcards.integration

import com.tauro.creditcards.model.CreditCardProtocol._

sealed trait GatewayError

final case class CsCardError(e: Throwable) extends GatewayError

final case class ScoredCardError(e: Throwable) extends GatewayError

trait CreditCardGateway[F[_]] {
  def fetch(req: CreditCardRequest): F[Either[GatewayError, List[GatewayResponse]]]
}
