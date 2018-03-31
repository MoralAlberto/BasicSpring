package com.example.basic.Service

import com.example.basic.Customer
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

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

    override fun hello(): String = "hello"

    override fun all(): Collection<Customer> =
            Customers.selectAll().map { Customer(it[Customers.name], it[Customers.id]) }

    override fun byId(id: Long): Customer? =
            Customers
                    .select { Customers.id.eq(id) }
                    .map { Customer(it[Customers.name], it[Customers.id]) }
                    .firstOrNull()

    override fun insert(c: Customer): Long {
        val value = Customers.insert { it[Customers.name] = c.name }
        return value.generatedKey!!.toLong()
    }
}

interface CustomerService {
    fun hello(): String
    fun all(): Collection<Customer>
    fun byId(id: Long): Customer?
    fun insert(c: Customer): Long
}