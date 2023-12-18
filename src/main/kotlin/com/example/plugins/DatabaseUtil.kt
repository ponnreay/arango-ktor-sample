package com.example.plugins

import com.arangodb.ArangoDB
import com.arangodb.ArangoDatabase
import com.arangodb.Protocol
import com.arangodb.entity.LoadBalancingStrategy
import io.ktor.server.config.*

object DatabaseUtil {
    private var connection: ArangoDB? = null
    private var arangoDb: ArangoDatabase? = null

    private fun getConnection(): ArangoDB {
        val config = AppConfig.getDbConfig()
        return connection ?: ArangoDB.Builder()
            .host(config.host, config.port)
            .user(config.username)
            .password(config.password)
            .maxConnections(config.maxConnection) // pooling
            .protocol(Protocol.HTTP_JSON)
            .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
            .build()
            .also {
                connection = it
            }
    }

    fun db(): ArangoDatabase {
        val config = AppConfig.getDbConfig()
        return arangoDb ?: getConnection().db(config.database)
    }
}

data class DbConfig(
    var host: String = "",
    var port: Int = 0,
    var username: String = "",
    var password: String = "",
    var database: String = "",
    var maxConnection: Int = 0,
)

object AppConfig {
    private var appConfig: ApplicationConfig? = null
    fun getDbConfig(): DbConfig {
        val config = appConfig ?: ApplicationConfig("application.yaml")
        return DbConfig(
            port = config.property("ktor.data-source.port").getString().toInt(),
            host = config.property("ktor.data-source.host").getString(),
            username = config.property("ktor.data-source.user").getString(),
            password = config.property("ktor.data-source.password").getString(),
            database = config.property("ktor.data-source.database").getString(),
            maxConnection = config.property("ktor.data-source.max-connection").getString().toInt(),
        )
    }
}