package com.paterake.elt.lake.cfg.transformation.model

case class ModelTransformOutputMapping(entityName: String
                                       , format: String
                                       , fileName: String
                                       , target: List[ModelTransformOutputMappingTarget]
                                      )

case class ModelTransformOutputMappingTarget(env: String
                                             , path: String
                                            )
