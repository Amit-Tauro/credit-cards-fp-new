package com.tauro.creditcards

import cats.effect.Async
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import com.tauro.creditcards.CreditCardProtocol._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.middleware.ResponseLogger

final case class CsCardError(e: Throwable) extends RuntimeException

final case class ScoredCardError(e: Throwable) extends RuntimeException


trait CreditCardGateway[F[_]] {
  def scoredCards(req: CreditCardRequest): F[Either[ScoredCardError, List[ScoredCardsResponse]]]
  def csCards(req: CreditCardRequest): F[Either[CsCardError, List[CsCardResponse]]]
}

class CreditCardGatewayImpl[F[_]: Async](client: Client[F]) extends CreditCardGateway[F] {
  val dsl = new Http4sClientDsl[F]{}
  import dsl._

  private val clientWithLogging = ResponseLogger(true, true)(client)

  override def csCards(req: CreditCardRequest): F[Either[CsCardError, List[CsCardResponse]]] = {
    val csReq: Request[F] = POST(uri"https://app.clearscore.com/api/global/backend-tech-test/v1/card").
        withEntity(CsCardRequest(req.name, req.creditScore))
    clientWithLogging.expect[List[CsCardResponse]](csReq).attempt.map(_.leftMap(e => CsCardError(e)))
  }

  override def scoredCards(req: CreditCardRequest): F[Either[ScoredCardError, List[ScoredCardsResponse]]] = {
    val scReq: Request[F] = POST(uri"https://app.clearscore.com/api/global/backend-tech-test/v2/creditcard").withEntity(
      ScoredCardsRequest(req.name, req.creditScore, req.salary))
    clientWithLogging.expect[List[ScoredCardsResponse]](scReq).attempt.map(_.leftMap(e => ScoredCardError(e)))
  }
}