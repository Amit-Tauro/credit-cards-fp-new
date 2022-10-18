package com.tauro.creditcards.integration

import cats.effect.Async
import cats.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import com.tauro.creditcards.model.GatewayProtocol._
import com.tauro.creditcards.model.CreditCardProtocol._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.middleware.ResponseLogger

import scala.util.Properties.envOrElse

class ScoredCards[F[_]: Async](client: Client[F]) extends CreditCardGateway[F] {
  val dsl = new Http4sClientDsl[F]{}
  import dsl._

  private val clientWithLogging = ResponseLogger(true, true)(client)

  override def fetch(req: CreditCardRequest): F[Either[GatewayError, List[GatewayResponse]]] = {
    val scoredCardsReq: Request[F] = POST(getScoredCardsEndpoint).withEntity(ScoredCardsRequest(req.name, req.creditScore, req.salary))
    clientWithLogging.expect[List[ScoredCardsResponse]](scoredCardsReq).attempt.map(_.leftMap(e => ScoredCardError(e)))
  }

  private def getScoredCardsEndpoint: Uri = {
    Uri.fromString(envOrElse("SCOREDCARDS_ENDPOINT", "https://app.clearscore.com/api/global/backend-tech-test/v2/creditcard"))
      .getOrElse(uri"https://app.clearscore.com/api/global/backend-tech-test/v2/creditcard")
  }
}