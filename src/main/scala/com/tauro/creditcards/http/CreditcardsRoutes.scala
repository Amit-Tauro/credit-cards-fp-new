package com.tauro.creditcards.http

import cats.effect.Concurrent
import com.tauro.creditcards.model.CreditCardProtocol.CreditCardRequest
import com.tauro.creditcards.service.CreditCardService
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import cats.syntax.all._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf


object CreditcardsRoutes {

  def creditCardRoutes[F[_] : Concurrent](creditCardService: CreditCardService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    implicit val creditCardDecoder: EntityDecoder[F, CreditCardRequest] = jsonOf[F, CreditCardRequest]
    HttpRoutes.of[F] {
      case req@POST -> Root / "creditcards" =>
        for {
          request <- req.as[CreditCardRequest]
          res <- creditCardService.fetchCards(request)
          resp <- Ok(res)
        } yield resp
    }
  }
}
