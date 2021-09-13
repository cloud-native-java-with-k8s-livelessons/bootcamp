package com.example.basicsreactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class BasicsReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasicsReactiveApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(CustomerRepository cr) {
		return RouterFunctions
			.route()
			.GET("/customers", r -> ok().body(cr.findAll(), Customer.class))
			.build();
	}

	@Bean
	ApplicationRunner runner(CustomerRepository cr) {
		return args -> {

			var names = Flux.just("A", "B", "C")
				.map(name -> new Customer(null, name))
				.flatMap(cr::save);

			names.subscribe();
		};
	}

}
/*

@Controller
@ResponseBody
@RequiredArgsConstructor
class CustomerRestController {

	private final CustomerRepository customerRepository;

	@GetMapping("/customers")
	Flux<Customer> get() {
		return this.customerRepository.findAll();
	}
}
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

	@Id
	private Integer id;

	private String name;
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

}