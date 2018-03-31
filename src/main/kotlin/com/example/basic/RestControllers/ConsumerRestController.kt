package com.example.basic.RestControllers

import com.example.basic.Customer
import com.example.basic.ResponseStatus.CustomerNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.example.basic.Service.CustomerService
import com.example.basic.guard
import java.net.URI

@RestController
class CustomerRestController(private val customerService: CustomerService) {

    @GetMapping("/")
    fun hello() = customerService.hello()

    @GetMapping("customers")
    fun customers() = customerService.all()

    @GetMapping("customer/{id}")
    fun customers(@PathVariable id: Long): Customer? {
        return customerService.byId(id).guard {
            throw CustomerNotFoundException(id.toString())
        }
    }

    @PutMapping("customer/{id}/{name}")
    fun customers(@PathVariable id: Long, @PathVariable name: String): ResponseEntity<Customer> {
        customerService.updateById(id, name)
        val updatedCustomer = customerService.byId(id).guard {
            throw CustomerNotFoundException(id.toString())
        }
        return ResponseEntity.ok(updatedCustomer!!)
    }

    @PostMapping("newCustomer")
    fun customers(@RequestBody(required = true) customer: Customer): ResponseEntity<Customer> {
        val newCustomer = Customer(customer.name)
        val id = customerService.insert(newCustomer)
        val findCustomer = customerService.byId(id).guard {
            throw CustomerNotFoundException(id.toString())
        }
        return ResponseEntity
                .created(URI.create("newCustomer"))
                .body(findCustomer!!)
    }
}