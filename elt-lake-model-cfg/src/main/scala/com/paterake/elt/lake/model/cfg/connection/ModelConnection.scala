package com.paterake.elt.lake.model.cfg.connection

import com.paterake.elt.lake.model.cfg.authentication.ModelAuthentication

trait ModelConnection {
  def position: Int
}

case class ModelConnectionDatabase(position: Int
                                   , driver: String
                                   , url: String
                                   , authentication: ModelAuthentication
                                  ) extends ModelConnection

case class ModelConnectionRest(position: Int
                               , authentication: ModelAuthentication
                              ) extends ModelConnection

case class ModelConnectionFileSystem(position: Int
                                     , authentication: ModelAuthentication
                                    ) extends ModelConnection
