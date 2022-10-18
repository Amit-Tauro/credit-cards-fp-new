package com.tauro.creditcards

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import com.tauro.creditcards.integration.{CsCards, ScoredCards}
import com.tauro.creditcards.model.GatewayProtocol.{CsCardResponse, ScoredCardsResponse}
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object CreditcardsServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      csCards = new CsCards[F, CsCardResponse](client)
      scoredCards = new ScoredCards[F, ScoredCardsResponse](client)
      transformerService = new TransformerServiceImpl
      creditCardService = new CreditCardServiceImpl[F](csCards, scoredCards, transformerService)
      creditCardRoutes = new CreditCardsRoutes[F](creditCardService)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        creditCardRoutes.creditCardRoutes
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
