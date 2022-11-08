package com.paterake.elt.lake.model.cfg.common

trait ModelConnection {def position: Int}

case class ModelConnectionDatabase(position: Int
                                     , driver: String
                                     , url: String
                                     , userName: String
                                     , password: String
                                     ) extends ModelConnection

case class ModelConnectionRest(position: Int) extends ModelConnection
