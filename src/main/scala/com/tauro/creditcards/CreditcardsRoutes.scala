package com.tauro.creditcards

import cats.effect.Concurrent
import cats.implicits._
import com.tauro.creditcards.CreditCardProtocol._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}


object CreditcardsRoutes {

  def creditCardRoutes[F[_]: Concurrent](creditCardService: CreditCardService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "creditcards" =>
        for {
          request <- req.as[CreditCardRequest]
          res <- creditCardService.fetchCards(request)
          resp <- Ok(res)
        } yield resp
    }
  }
}