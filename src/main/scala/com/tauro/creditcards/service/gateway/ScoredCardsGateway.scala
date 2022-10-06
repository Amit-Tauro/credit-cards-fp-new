package com.tauro.creditcards.service.gateway

import cats.effect.kernel.Concurrent
import com.tauro.creditcards.model.CreditCardProtocol.{ScoredCardsRequest, ScoredCardsResponse}
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

class ScoredCardsGateway[F[_] : Concurrent](client: Client[F]) extends CreditCardGateway[ScoredCardsRequest, ScoredCardsResponse, F](client) {
  override val url: Uri = uri"https://app.clearscore.com/api/global/backend-tech-test/v2/creditcards"
}
