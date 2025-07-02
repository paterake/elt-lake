package com.paterake.elt.lake.cfg.ingestion

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, DefaultCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest

class ParameterStore(region: String) {
  private val log = LoggerFactory.getLogger(getClass.getName)

  private val chain = AwsCredentialsProviderChain
    .of(ProfileCredentialsProvider.builder().build(), DefaultCredentialsProvider.create())
    
  private val ssmClient = SsmClient.builder()
    .credentialsProvider(chain)
    .region(Region.of(region))
    .build()

  def getParameter(parameterKey: String): String = {
    log.warn("Retrieving value for parameter: " + parameterKey)
    val parameterRequest = GetParameterRequest.builder()
      .name(parameterKey)
      .withDecryption(true)
      .build()
    val parameterResponse = ssmClient.getParameter(parameterRequest)
    parameterResponse.parameter().value()
  }

  def getParameter(keyPrefix: String, key: String): String = {
    val parameterKey = String.format("%s/%s", keyPrefix, key.replaceAll("\\.", "/"))
    getParameter(parameterKey)
  }
}

object ParameterStore {
  private val log = LoggerFactory.getLogger(getClass.getName)
  @volatile private var instance: Option[ParameterStore] = None
  
  def getInstance: ParameterStore = synchronized {
    instance match {
      case Some(store) => store
      case None =>
        val defaultCfg = new IngestCfg("edp").getConfig(indResolveSecret = false, null, "parameterStore")
        val store = new ParameterStore(defaultCfg("region"))
        instance = Some(store)
        store
    }
  }

  def getParameter(keyPrefix: String, key: String): String = {
    getInstance.getParameter(keyPrefix, key)
  }

  def main(args: Array[String]): Unit = {
    val value = getParameter(args(0), args(1))
    log.warn(value)
  }
}
