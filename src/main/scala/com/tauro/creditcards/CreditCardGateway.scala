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
import scala.util.Properties.envOrElse

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
    val csCardsReq: Request[F] = POST(getCsCardsEndpoint).withEntity(CsCardRequest(req.name, req.creditScore))
    println(System.currentTimeMillis())
    clientWithLogging.expect[List[CsCardResponse]](csCardsReq).attempt.map(_.leftMap(e => CsCardError(e))).map { value =>
      println(s"[${Thread.currentThread().getName}] $value")
      value
    }
  }

  override def scoredCards(req: CreditCardRequest): F[Either[ScoredCardError, List[ScoredCardsResponse]]] = {
    val scoredCardsReq: Request[F] = POST(getScoredCardsEndpoint).withEntity(ScoredCardsRequest(req.name, req.creditScore, req.salary))
    println(System.currentTimeMillis())
    clientWithLogging.expect[List[ScoredCardsResponse]](scoredCardsReq).attempt.map(_.leftMap(e => ScoredCardError(e))).map { value =>
      println(s"[${Thread.currentThread().getName}] $value")
      value
    }
  }

  private def getCsCardsEndpoint: Uri = {
    Uri.fromString(envOrElse("CSCARDS_ENDPOINT", "https://app.clearscore.com/api/global/backend-tech-test/v1/card"))
      .getOrElse(uri"https://app.clearscore.com/api/global/backend-tech-test/v1/card")
  }

  private def getScoredCardsEndpoint: Uri = {
    Uri.fromString(envOrElse("SCOREDCARDS_ENDPOINT", "https://app.clearscore.com/api/global/backend-tech-test/v2/creditcard"))
      .getOrElse(uri"https://app.clearscore.com/api/global/backend-tech-test/v2/creditcard")
  }
}