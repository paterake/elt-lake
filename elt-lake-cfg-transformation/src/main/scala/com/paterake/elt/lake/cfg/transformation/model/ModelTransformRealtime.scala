package com.paterake.elt.lake.cfg.transformation.model

case class ModelTransformRealtime(entityName: String
                                  , sourceName: String
                                  , transform: List[ModelTransformRealtimeSql])

case class ModelTransformRealtimeSql(order: Int
                                     , targetEntityName: String
                                     , transformSql: String
                                     , schemaMapping: String
                                     , sqlText: String = ""
                                     , schema: StructType = null
                                    )

