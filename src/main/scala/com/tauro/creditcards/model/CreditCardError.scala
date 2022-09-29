package com.tauro.creditcards.model

sealed abstract class CreditCardError(message: String, cause: Throwable = null) // scalastyle:ignore null
    extends Exception(message, cause)

object CreditCardError {

  case class CreditCardPartnerCallError(cause: Throwable) extends CreditCardError("Serialization", cause)
}
