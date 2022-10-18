package com.tauro.creditcards.integration

import com.tauro.creditcards.model.CreditCardProtocol._
import org.http4s.EntityDecoder

sealed trait GatewayError

final case class CsCardError(e: Throwable) extends GatewayError

final case class ScoredCardError(e: Throwable) extends GatewayError

trait CreditCardGateway[F[_], A] {
  def fetch(req: CreditCardRequest)(implicit entityDecoder: EntityDecoder[F, List[A]]): F[Either[GatewayError, List[A]]]
}
