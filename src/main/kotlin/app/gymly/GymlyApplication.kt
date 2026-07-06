package app.gymly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class GymlyApplication

fun main(args: Array<String>) {
    runApplication<GymlyApplication>(*args)
}
