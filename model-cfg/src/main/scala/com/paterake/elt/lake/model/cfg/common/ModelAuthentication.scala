package com.paterake.elt.lake.model.cfg.common

trait ModelAuthentication {}

case class ModelAuthenticationUserName( userName: String
                                      , password: String
                                      ) extends ModelAuthentication

case class ModelAuthenticationBearerToken(token: String) extends ModelAuthentication

case class ModelAuthenticationAWS(accessKey: String
                                 , secretKey: String
                                 ) extends ModelAuthentication
