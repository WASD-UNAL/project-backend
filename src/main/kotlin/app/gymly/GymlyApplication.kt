package app.gymly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GymlyApplication

fun main(args: Array<String>) {
	runApplication<GymlyApplication>(*args)
}
