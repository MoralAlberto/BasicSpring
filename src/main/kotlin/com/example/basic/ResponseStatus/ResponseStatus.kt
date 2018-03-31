package com.example.basic.ResponseStatus

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CustomerNotFoundException(val id: String): RuntimeException("User with ${id} not found")