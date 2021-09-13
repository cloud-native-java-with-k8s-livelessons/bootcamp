package com.example.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.Objects;


public class Application {

	public static void main(String[] args) {
		var annotationConfigApplicationContext =
			new AnnotationConfigApplicationContext(ApplicationConfiguration.class);

		// initialize schema

		var jdbc = annotationConfigApplicationContext.getBean(JdbcTemplate.class);
		jdbc.execute("create table customer(id serial primary key , name varchar (255) not null)");

		// use the customerService

		var cs = annotationConfigApplicationContext.getBean(CustomerService.class);
		var k = cs.save("Kimly");
		System.out.println(k.toString());
	}

}

interface CustomerService {
	Customer save(String name);

	Customer findById(Integer id);
}

@ComponentScan
@EnableTransactionManagement
@Configuration
class ApplicationConfiguration {

	@Bean
	JdbcTransactionManager jdbcTransactionManager(DataSource dataSource) {
		return new JdbcTransactionManager(dataSource);
	}

	@Bean
	DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.H2)
			.build();
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

	private Integer id;
	private String name;
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