package com.tauro.creditcards.service.gateway

import cats.effect.Concurrent
import cats.implicits.catsSyntaxMonadError
import com.tauro.creditcards.model.CreditCardError.CreditCardPartnerCallError
import com.tauro.creditcards.model.CreditCardProtocol
import com.tauro.creditcards.model.CreditCardProtocol._
import com.tauro.creditcards.service.gateway.CreditCardGateway.{RequestTransformer, TransformerEnrichment}
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, EntityEncoder, Request, Uri}

abstract class CreditCardGateway[O, T, F[_] : Concurrent](client: Client[F]) {

  implicit val transformer: RequestTransformer[O]

  def url: Uri

  def fetchCards(req: CreditCardProtocol.CreditCardRequest)
                (implicit encoder: EntityEncoder[F, O], decoder: EntityDecoder[F, List[T]]): F[List[T]] = {
    val dsl = new Http4sClientDsl[F] {}

    import dsl._
    val csReq: Request[F] = POST(url).withEntity(req.transform)
    client.expect[List[T]](csReq)
      //      .attempt
      .adaptError { case t =>
        CreditCardPartnerCallError(t)
      }
    //      .map {
    //        case Right(x) => x.asRight[CreditCardError]
    //        case Left(e: Throwable) => CreditCardPartnerCallError(e).asLeft
    //      }  }  }
  }
}

object CreditCardGateway {

  implicit class TransformerEnrichment(value: CreditCardRequest) {
    def transform[O](implicit transformer: RequestTransformer[O]): O = transformer.transform(value)
  }

  trait RequestTransformer[O] {
    def transform(value: CreditCardRequest): O
  }
}
