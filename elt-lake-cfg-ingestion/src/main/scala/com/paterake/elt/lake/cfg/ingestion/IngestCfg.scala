package com.paterake.elt.lake.cfg.ingestion

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import org.slf4j.LoggerFactory

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, ZoneId}
import scala.collection.mutable

class IngestCfg(sourceName: String, configName: String, domainType: String) {
  import scala.collection.JavaConverters._

  private val log = LoggerFactory.getLogger(getClass.getName)

  def this(sourceName: String) {
    this(sourceName, "application.conf", "ingest")
  }

  def this(sourceName: String, configName: String) {
    this(sourceName, configName, "ingest")
  }

  val domain: String = String.format("edp.%s.cfg", domainType)

  // Check if resource exists before trying to parse it
  private val resourcePath = String.format("%s/%s/%s", domainType, sourceName, configName)
  private val resourceStream = getClass.getClassLoader.getResourceAsStream(resourcePath)
  if (resourceStream == null) {
    val errorMsg = s"Configuration file not found: $resourcePath"
    log.error(errorMsg)
    throw new IllegalArgumentException(errorMsg)
  }
  resourceStream.close()

  val config: Config = try {
    ConfigFactory.parseResources(resourcePath)
  } catch {
    case e: Exception =>
      val errorMsg = s"Failed to parse configuration file $resourcePath: ${e.getMessage}"
      log.error(errorMsg)
      throw new IllegalArgumentException(errorMsg, e)
  }

  //val config: Config = ConfigFactory.parseResources(String.format("%s/%s/%s", domainType, sourceName, configName))

  def getHoconCfg: (String, Config) = {
    (domain, config)
  }

  def getJsonCfg: String = {
    val jsonString = ConfigFactory
      .parseResources(String.format("%s/%s/%s", domainType, sourceName, configName))
      .root()
      .render(ConfigRenderOptions.concise())
    jsonString
  }

  def checkConfig(configKey: String): Boolean = {
    val ind: Boolean =
      try {
        config.getConfig(String.format("%s.%s", domain, configKey))
        true
      } catch {
        case _: Exception => false
      }
    ind
  }

  def substituteToken(token: String): String = {
    if (token.contains("/")) {
      val clcnToken = token.split("/")
        .flatMap(x => x.split("="))
        .grouped(2).map(a => a(0) -> a(1)).toMap
      var returnValue = ""
      if (clcnToken.contains("format")) {
        val formattedDate = DateTimeFormatter.ofPattern(clcnToken("format"))
          .withZone(ZoneId.of("UTC"))
          .format(Instant.now())
        val date = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern(clcnToken("format")))
          .minusDays(clcnToken("offset").toInt)
        returnValue = DateTimeFormatter.ofPattern(clcnToken("format"))
          .withZone(ZoneId.of("UTC"))
          .format(date)
      }
      returnValue
    } else {
      val clcnSystemProp = System.getProperties
      if (clcnSystemProp.containsKey(s"{$token}")) {
        clcnSystemProp.get(s"{$token}").toString
      } else {
        token
      }
    }
  }

  def getConfig(indResolveSecret: Boolean, keyPrefix: String, configKey: String): Map[String, String] = {
    val parameterKey = String.format("%s.%s", domain, configKey)
    val clcnCfgStatic: Map[String, String] = config.getConfig(parameterKey).entrySet().asScala.map(x => (x.getKey, x.getValue.unwrapped().toString)).toMap
    val clcnCfg: mutable.Map[String, String] = collection.mutable.Map(clcnCfgStatic.toSeq: _*)
    val clcnEnv = System.getenv()
    clcnCfg.foreach(x => {
      if (clcnEnv.containsKey(x._2)) {
        clcnCfg(x._1) = clcnEnv.get(x._2)
      }
    })
    if (indResolveSecret) {
      lazy val parameterStore = ParameterStore.getInstance
      
      clcnCfg.foreach(kv => {
        if ("ssm-key".equalsIgnoreCase(kv._2)) {
          val ssmKey = (parameterKey + "/" + kv._1).replace(".source", "." + sourceName)
          try {
            clcnCfg(kv._1) = parameterStore.getParameter(keyPrefix, ssmKey)
          } catch {
            case _: Exception =>
              log.warn("Parameter not found: " + kv._1 + ":" + keyPrefix + ":" + ssmKey)
              throw new Exception("Parameter not found: " + kv._1 + ":" + keyPrefix + ":" + ssmKey)
          }
        }
      })
    }
    clcnCfg.foreach(x => {
      if (x._2.contains("${")) {
        var keyValue = x._2
        "(?<=\\{)[^}]+(?=\\})".r.findAllIn(x._2).foreach(v => {
          keyValue = keyValue.replace("${" + v + "}", substituteToken(v))
        })
        clcnCfg(x._1) = keyValue
      }
    })
    clcnCfg.toMap
  }

  def getConfigList(configKey: String): Option[List[String]] = {
    try {
      val clcn = config.getStringList(String.format("%s.%s", domain, configKey))
      Some(clcn.asScala.toList)
    } catch {
      case _: Exception => None
    }
  }

  def getConfigListMap(configKey: String): Option[List[Map[String, String]]] = {
    def toMap(hashMap: AnyRef): Map[String, AnyRef] = hashMap.asInstanceOf[java.util.Map[String, AnyRef]].asScala.toMap

    def toList(list: AnyRef): List[AnyRef] = list.asInstanceOf[java.util.List[AnyRef]].asScala.toList

    try {
      val clcn = config.getList(String.format("%s.%s", domain, configKey))
        .unwrapped()
        .asScala.toList
        .map(x => toMap(x).map { case (k, v) => k -> v.toString })
      Some(clcn)
    } catch {
      case _: Exception => None
    }
  }

  def getBucketName(clcnCfg: Map[String, String]): String = {
    val bucketName = if (clcnCfg.contains("bucket.name")) {
      clcnCfg("bucket.name")
    } else {
      clcnCfg("secure.bucket.name")
    }
    bucketName
  }

  private def getEntityName(clcnCfg: Map[String, String], sourceName: String, entityName: String): String = {
    val tgtEntityName = if (clcnCfg.contains("prepend.source") && "Y".equalsIgnoreCase(clcnCfg("prepend.source"))) {
      String.format("%s_%s", sourceName, entityName)
    } else {
      entityName
    }
    tgtEntityName
  }

  def getCfgTargetPath(indResolveSecret: Boolean, keyPrefix: String): Map[String, String] = {
    getConfig(indResolveSecret, keyPrefix, "target.s3")
  }

  def getTemplatePath(clcnTarget: Map[String, String]): String = {
    val bucketName = getBucketName(clcnTarget)
    val templatePath = String.format("s3://%s/%s", bucketName, clcnTarget("level.1.path"))
    log.warn("Template Path: " + templatePath)
    templatePath
  }

  def getTargetPath(clcnTarget: Map[String, String], sourceName: String, entityName: String): String = {
    val templatePath = getTemplatePath(clcnTarget)
    val formattedDate = DateTimeFormatter.ofPattern("yyyyMMddHH")
      .withZone(ZoneId.of("UTC"))
      .format(Instant.now())
    val tgtEntityName = getEntityName(clcnTarget, sourceName, entityName)

    val path = String.format(templatePath, clcnTarget("env").toLowerCase, tgtEntityName.toLowerCase, sourceName.toLowerCase, formattedDate.substring(0, 8))
    log.warn("Level 1 persist: " + path)
    path
  }

  def getTargetPath(indResolveSecret: Boolean, keyPrefix: String, sourceName: String, entityName: String): String = {
    val clcnTarget = getCfgTargetPath(indResolveSecret, keyPrefix)
    val path = getTargetPath(clcnTarget, sourceName, entityName)
    path
  }

  def getClcnEntity(clcnSourceEntity: Map[String, String], clcnMapInput: Map[String, String]): List[String] = {
    val clcnEntityName = clcnSourceEntity.map(x => x._1.split("\\.").head).toList.distinct
    val clcnEntity =
      if (clcnMapInput != null && clcnMapInput.contains("entityName")) {
        clcnEntityName.filter(p => p.equalsIgnoreCase(clcnMapInput("entityName")))
      } else {
        clcnEntityName
      }
    clcnEntity
  }

  def getClcnEntity(indResolve: Boolean, keyPrefix: String, entityKey: String, clcnMapInput: Map[String, String]): List[String] = {
    val clcnSourceEntity = getConfig(indResolve, keyPrefix, entityKey)
    val clcnEntity = getClcnEntity(clcnSourceEntity, clcnMapInput)
    clcnEntity
  }
}

object IngestCfg {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def extractCfg(env: String, keyPrefix: String, sourceName: String, configName: String, entityName: String): ModelIngestCfg = {
    val cfgKey = String.format("%s:%s:%s:%s:%s", env, keyPrefix, sourceName, configName, entityName)
    log.warn("Processing: " + cfgKey)
    val ingestCfg = new IngestCfg(sourceName, configName)
    if (!ingestCfg.config.isEmpty) {
      ModelIngestCfg(ingestCfg, env, keyPrefix, sourceName, entityName)
    } else {
      throw new Exception("Config missing for key: " + cfgKey)
    }
  }

  def extractCfg(env: String, keyPrefix: String, sourceName: String): ModelIngestCfg = {
    extractCfg(env, keyPrefix, sourceName, "application.conf", null)
  }

  def getCfg(clcnArg: Array[String]): ModelIngestCfg = {
    if (clcnArg.length == 3) {
      extractCfg(clcnArg(0), clcnArg(1), clcnArg(2))
    } else if (clcnArg.length == 4) {
      extractCfg(clcnArg(0), clcnArg(1), clcnArg(2), clcnArg(3), null)
    } else if (clcnArg.length == 5) {
      extractCfg(clcnArg(0), clcnArg(1), clcnArg(2), clcnArg(3), clcnArg(4))
    } else {
      throw new Exception("Require: env keyPrefix sourceName [configName [entityName]]")
    }
  }
}