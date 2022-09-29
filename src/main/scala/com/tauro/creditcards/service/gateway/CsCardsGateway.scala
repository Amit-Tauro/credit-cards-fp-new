package com.tauro.creditcards.service.gateway

import cats.effect.Concurrent
import com.tauro.creditcards.model.CreditCardProtocol.{CreditCardRequest, CsCardRequest, CsCardResponse}
import com.tauro.creditcards.service.gateway.CreditCardGateway.RequestTransformer
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

class CsCardsGateway[F[_] : Concurrent](client: Client[F]) extends CreditCardGateway[CsCardRequest, CsCardResponse, F](client) {
  override val url: Uri = uri"https://app.clearscore.com/api/global/backend-tech-test/v1/cards"
  override implicit val transformer: RequestTransformer[CsCardRequest] = (req: CreditCardRequest) => CsCardRequest(req.name, req.creditScore)
}