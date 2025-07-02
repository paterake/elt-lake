package com.paterake.elt.lake.cfg.ingestion

case class ModelIngestCfg(ingestCfg: IngestCfg
                          , env: String
                          , keyPrefix: String
                          , sourceName: String
                          , entityName: String
                         )
