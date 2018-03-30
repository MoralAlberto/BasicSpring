package com.example.basic

import com.intellij.util.ObjectUtils.assertNotNull
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.web.WebAppConfiguration


@SpringBootTest
@WebAppConfiguration
class BasicApplicationTests {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun contextLoads() {
    }

    @Test
    fun whenCalled_shouldReturnHello() {
        val result = testRestTemplate
                .getForEntity("/", String::class.java)

        assertNotNull(result)
    }

}
