package com.example.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.PreparedStatement;
import java.util.Objects;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ApplicationRunner runner(CustomerService customerService) {
		return args -> System.out.println(customerService.save("Tammie"));
	}

}

interface CustomerService {
	Customer save(String name);

	Customer findById(Integer id);
}

@Controller
@RequiredArgsConstructor
@ResponseBody
class CustomerRestController {

	private final CustomerService customerService;

	@GetMapping("/customers/{id}")
	Customer byId(@PathVariable Integer id) {
		return this.customerService.findById(id);
	}
}

@Service
@RequiredArgsConstructor
@Transactional
class JdbcCustomerService implements CustomerService {

	private final JdbcTemplate template;

	@Override
	public Customer save(String name) {
		var gkh = new GeneratedKeyHolder();
		this.template.update(connection -> {
			var ps = connection
				.prepareStatement("insert into customer(name) values(?)", PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString(1, name);
			return ps;
		}, gkh);
		var id = (Number) (Objects.requireNonNull(gkh.getKeys())).values().iterator().next();
		return findById(id.intValue());
	}

	@Override
	public Customer findById(Integer id) {
		var q = "select * from customer where id = ? ";
		return template.queryForObject(q, (rs, i) -> new Customer(rs.getInt("id"), rs.getString("name")), id);
	}
}