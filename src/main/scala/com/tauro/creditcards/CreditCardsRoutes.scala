package com.tauro.creditcards

import cats.effect.Concurrent
import cats.implicits._
import com.tauro.creditcards.model.CreditCardProtocol._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}


class CreditCardsRoutes[F[_]: Concurrent](creditCardService: CreditCardService[F]) extends Http4sDsl[F] {

  private val handler: RouteHandler[F] = new RouteHandler[F]

  def creditCardRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "creditcards" =>
        handler.handle(
          for {
            request <- req.as[CreditCardRequest]
            res <- creditCardService.fetchCards(request)
            resp <- Ok(res)
          } yield resp
        )
    }
  }
}