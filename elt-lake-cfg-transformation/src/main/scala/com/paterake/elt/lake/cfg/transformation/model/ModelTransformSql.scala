package com.paterake.elt.lake.cfg.transformation.model

case class ModelTransformSql(transformName: String
                             , description: String
                             , target: ModelTransformSqlTarget
                             , schedule: ModelTransformSqlSchedule
                             , sourceList: List[ModelTransformSqlSource]
                             , attributeList: List[ModelTransformSqlAttribute]
                            )

case class ModelTransformSqlTarget(level: String
                                   , entityName: String
                                   , sourceName: String
                                   , edpInsertDate: String
                                   , saveMode: String
                                   , transformSql: String
                                   , writePrefix: String
                                   , syncInd: String
                                   , targetFormat: String
                                   , targetDelimiter: String
                                   , targetQuoteInd: Boolean
                                   , targetHeader: String
                                   , targetCompression: String
                                   , fileNamePrefix: String
                                   , fileNameSuffix: String
                                   , fileNameDateOffset: Int
                                   , generateCheckSumInd: Boolean
                                   , checkSourceInd: Boolean
                                   , checkSourceRepeat: Int
                                   , checkSourceSleep: Int
                                  )

case class ModelTransformSqlSchedule(periodFrequency: String
                                     , periodUnit: String
                                     , periodStartTime: String
                                     , periodStartDate: String
                                    )

case class ModelTransformSqlSource(sourceKey: String
                                   , level: String
                                   , entityName: String
                                   , sourceName: String
                                   , edpInsertDate: String
                                  )

case class ModelTransformSqlAttribute(attributeName: String
                                      , attributeScope: String
                                     )
