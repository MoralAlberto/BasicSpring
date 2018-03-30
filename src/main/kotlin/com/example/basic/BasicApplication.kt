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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

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

    override fun all(): Collection<Customer> = Customers.selectAll().map { Customer(it[Customers.name], it[Customers.id]) }

    override fun byId(id: Long): Customer? =
            Customers
                    .select { Customers.id.eq(id) }
                    .map { Customer(it[Customers.name], it[Customers.id]) }
                    .firstOrNull()

    override fun insert(c: Customer) {
        Customers.insert { it[Customers.name] = c.name }
    }
}

@Profile("jdbc")
@Service
@Transactional
class JdbcCustomerService(private val jdbcOperations: JdbcOperations): CustomerService {
    override fun all(): Collection<Customer> = this.jdbcOperations.query("SELECT * FROM CUSTOMERS") { rs, _ ->
        Customer(rs.getString("NAME"), rs.getLong("ID"))
    }

    override fun byId(id: Long): Customer? = this.jdbcOperations.queryForObject("SELECT * FROM CUSTOMERS where ?", id) { rs, _ ->
        Customer(rs.getString("NAME"), rs.getLong("ID"))
    }

    override fun insert(c: Customer) {
        this.jdbcOperations.execute("INSERT into CUSTOMERS (NAME) values(?)") {
            it.setString(1, c.name)
            it.execute()
        }
    }
}

interface CustomerService {
    fun all(): Collection<Customer>
    fun byId(id: Long): Customer?
    fun insert(c: Customer)
}

data class Customer (var name: String, var id: Long? = null)
