package com.paterake.elt.lake.model.cfg.authentication

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}


@JsonTypeInfo(use = Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes(Array(
  new Type(value = classOf[ModelAuthenticationUserName], name = "userName"),
  new Type(value = classOf[ModelAuthenticationBearerToken], name = "bearerToken"),
  new Type(value = classOf[ModelAuthenticationAWS], name = "aws")
))
trait ModelAuthentication {}

case class ModelAuthenticationUserName(userName: String
                                       , password: String
                                      ) extends ModelAuthentication

case class ModelAuthenticationBearerToken(token: String) extends ModelAuthentication

case class ModelAuthenticationAWS(accessKey: String
                                  , secretKey: String
                                 ) extends ModelAuthentication
