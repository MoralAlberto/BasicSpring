package com.example.basic

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.support.beans
import com.example.basic.Service.CustomerService

@SpringBootApplication
class BasicApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(BasicApplication::class.java)
            .initializers(beans {

                bean {
                    SpringTransactionManager(ref())
                }

                bean {
                    ApplicationRunner {
                        val customerService = ref<CustomerService>()
                        arrayOf("Alberto", "Diana")
                                .map { Customer(name = it) }
                                .forEach { customerService.insert(it) }
                        customerService.all().forEach { println(it) }
                    }
                }
            })
            .run(*args)
}

data class Customer (var name: String, var id: Long? = null)

inline fun <T> T.guard(block: T.() -> Unit): T {
    if (this == null) block(); return this
}