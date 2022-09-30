package com.tauro.creditcards
import cats.effect.kernel.Concurrent
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.{InvalidMessageBodyFailure, MalformedMessageBodyFailure, Response}



class RouteHandler[F[_] : Concurrent] extends Http4sDsl[F] {

  private val errorHandler: PartialFunction[Throwable, F[Response[F]]] = {
    case _: InvalidMessageBodyFailure => BadRequest()
    case _: MalformedMessageBodyFailure => BadRequest()
    case GatewayError(msg) => InternalServerError(msg)
    case _ => InternalServerError()
  }

  def handle(response: F[Response[F]]): F[Response[F]] = {
    response.recoverWith { case e => errorHandler(e) }
  }

}
