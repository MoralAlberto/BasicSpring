package com.example.basic


import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Profile
import org.springframework.context.support.beans
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.script.ScriptTemplateConfig
import org.springframework.web.servlet.view.script.ScriptTemplateConfigurer
import org.springframework.web.servlet.view.script.ScriptTemplateViewResolver

@SpringBootApplication
class BasicApplication

fun main(args: Array<String>) {
    // runApplication<BasicApplication>(*args)
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

@RestController
class CustomerRestController(private val customerService: CustomerService) {
    @GetMapping("customers")
    fun customers() = customerService.all()

    @GetMapping("customer/{id}")
    fun customers(@PathVariable id: Long) = customerService.byId(id)

    @PostMapping("new")
    fun customers(@RequestBody customer: Customer) {
        val newCustomer = Customer(customer.name)
        customerService.insert(newCustomer)
    }
}

object Customers: Table() {
    val id = long("id").autoIncrement().primaryKey()
    val name = varchar("name", 255)
}

@Service
@Transactional
class ExposedCustomerService(private val transactionTemplate: TransactionTemplate): CustomerService, InitializingBean {
    override fun afterPropertiesSet() {
        transactionTemplate.execute {
            SchemaUtils.create(Customers)
        }
    }

    override fun all(): Collection<Customer> =
            Customers.selectAll().map { Customer(it[Customers.name], it[Customers.id]) }

    override fun byId(id: Long): Customer? =
            Customers
                    .select { Customers.id.eq(id) }
                    .map { Customer(it[Customers.name], it[Customers.id]) }
                    .firstOrNull()

    override fun insert(c: Customer) {
            Customers.insert { it[Customers.name] = c.name }
    }
}

interface CustomerService {
    fun all(): Collection<Customer>
    fun byId(id: Long): Customer?
    fun insert(c: Customer)
}

data class Customer (var name: String, var id: Long? = null)
