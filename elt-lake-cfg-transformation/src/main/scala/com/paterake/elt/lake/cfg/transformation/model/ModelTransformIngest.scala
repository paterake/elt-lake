package com.paterake.elt.lake.cfg.transformation.model

case class ModelTransformIngest(entityName: String
                                , sourceName: String
                                , saveMode: String
                                , format: String
                                , compression: String
                                , charset: String
                                , rootTag: String
                                , rowTag: String
                                , messageEnvelope: String
                                , messageEnvelopeFormat: String
                                , messageEnvelopeSchema: String
                                , messageRootTag: String
                                , messageRowTag: String
                                , delimiter: String
                                , header: Boolean
                                , multiLine: Boolean
                                , multiDocument: Boolean
                                , skip: Int
                                , trailerSkip: Int
                                , fileNameFormat: String
                                , columnSchema: String
                               )
