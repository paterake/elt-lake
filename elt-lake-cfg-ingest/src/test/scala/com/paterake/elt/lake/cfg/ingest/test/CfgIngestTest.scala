package com.paterake.elt.lake.cfg.ingest.test

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.paterake.elt.lake.model.cfg.connection.ModelConnectionRest
import org.scalatest.funsuite.AnyFunSuite

class CfgIngestTest extends AnyFunSuite {

  test("Config reader") {

    val jsonString = """{"position":1,"authentication":{"type":"userName", "userName":"abc","password":"123"}}"""

    val mapper = JsonMapper.builder()
      .addModule(DefaultScalaModule)
      .build()

    val myMap = mapper.readValue(jsonString, classOf[ModelConnectionRest])

    println(myMap.getClass.getName)

    println(myMap.authentication.getClass.getName)
  }

}
