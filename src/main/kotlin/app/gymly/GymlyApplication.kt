package app.gymly

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
	(
	exclude = [
		DataSourceAutoConfiguration::class,
		SecurityAutoConfiguration::class
	]
)
@EnableJpaRepositories(basePackages = ["app.gymly.repository"])

class GymlyApplication

fun main(args: Array<String>) {
	runApplication<GymlyApplication>(*args)
}
