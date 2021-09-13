package com.example.kotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class KotlinApplication

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args) {

        val context = beans {
            bean {
                val cr = ref<CustomerRepository>()
                router {
                    GET("/customers") {
                        ServerResponse.ok().body(cr.findAll())
                    }
                }
            }
        }
        addInitializers(context)
    }
}

interface CustomerRepository : ReactiveCrudRepository<Customer, Int>
data class Customer(@Id val id: Int, val name: String)