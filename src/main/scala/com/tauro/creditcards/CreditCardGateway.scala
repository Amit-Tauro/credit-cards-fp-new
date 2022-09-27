package com.tauro.creditcards

import cats.effect.Concurrent
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import com.tauro.creditcards.CreditCardProtocol._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}

final case class CsCardError(e: Throwable) extends RuntimeException

final case class ScoredCardError(e: Throwable) extends RuntimeException


trait CreditCardGateway[F[_]] {
  def scoredCards(req: CreditCardRequest): F[List[ScoredCardsResponse]]
  def csCards(req: CreditCardRequest): F[List[CsCardResponse]]
}

class CreditCardGatewayImpl[F[_]: Concurrent](client: Client[F]) extends CreditCardGateway[F] {
  val dsl = new Http4sClientDsl[F]{}
  import dsl._

  override def csCards(req: CreditCardRequest): F[List[CsCardResponse]] = {
    val csReq: Request[F] = POST(uri"https://app.clearscore.com/api/global/backend-tech-test/v1/cards").
        withEntity(CsCardRequest(req.name, req.creditScore))
    client.expect[List[CsCardResponse]](csReq).handleError(_ => List.empty)

//    client.expect[List[CsCardResponse]](csReq).adaptError{ case t => CsCardError(t)}// Prevent Client Json Decoding Failure Leaking
  }

  override def scoredCards(req: CreditCardRequest): F[List[ScoredCardsResponse]] = {
    val scReq: Request[F] = POST(uri"https://app.clearscore.com/api/global/backend-tech-test/v2/creditcards").withEntity(
      ScoredCardsRequest(req.name, req.creditScore, req.salary))
    client.expect[List[ScoredCardsResponse]](scReq).handleError(_ => List.empty)
  }
}