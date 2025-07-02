package com.paterake.elt.lake.cfg.ingestion.test

import com.paterake.elt.lake.cfg.ingestion.IngestCfg
import org.scalatest.funsuite.AnyFunSuite

class CfgIngestTest extends AnyFunSuite {

  val env = "dev"
  val keyPrefix: String = String.format("/%s", env)

  test("findatender cfg") {
    val sourceName = "findatender"
    val configName = "application.conf"
    val cfg = new IngestCfg(sourceName, configName)
    val cfgEntity = cfg.getConfig(false, keyPrefix, "source.rest.entity")
    println(cfgEntity)
  }

}
