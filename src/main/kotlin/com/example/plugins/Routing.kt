package com.example.plugins

import com.arangodb.entity.BaseDocument
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class User(
    var ref: Int,
    var name: String,
    var dob: String,
    var phone: String,
    var address: String
)

fun Application.configureRouting() {
    routing {

        get("/") {
            call.respondText("Hello World!")
        }

        /*
            curl http://localhost:8080/users | jq
         */
        get("/users") {
            val users = DatabaseUtil.db().query("FOR i IN users SORT i.ref ASC RETURN i", BaseDocument::class.java).asListRemaining()
            call.respond(users)
        }

        /*
            curl http://localhost:8080/users/1 | jq
         */
        get("/users/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()
            val user = DatabaseUtil.db().query(
                "FOR i IN users FILTER i.ref == @userId RETURN i",
                BaseDocument::class.java,
                mapOf("userId" to userId),
            ).asListRemaining().firstOrNull() ?: throw Exception("User[$userId] not found")
            call.respond(user)
        }

        /*
         curl -X POST \
           -H "Content-Type: application/json" \
           -d '{"name": "Alice Johnson", "dob": "2001-01-01", "phone": "+855123123123", "address": "Phnom Penh"}' \
           http://localhost:8080/users | jq
         */
        post("/users") {
            val user = call.receive<User>()
            user.ref = getIdForNewUser()
            val insertedUser = DatabaseUtil.db().query(
                "INSERT @user INTO users RETURN NEW",
                BaseDocument::class.java,
                mapOf("user" to user)
            ).asListRemaining().firstOrNull() ?: throw Exception("Error creating new user")
            call.respond(insertedUser)
        }
    }
}

private fun getIdForNewUser(): Int {
    return DatabaseUtil.db().query(
        """
            LET last = (
                FOR i IN users
                  SORT i.ref DESC
                  LIMIT 1
                  RETURN i
              )
            RETURN LENGTH(last) > 0 ? (last[0].ref + 1): 1
        """,
        Int::class.java
    ).asListRemaining().first()
}