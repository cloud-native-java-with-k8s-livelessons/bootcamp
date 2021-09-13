package com.example.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class ApplicationWiringConfiguration {

	@Bean
	MyDataSource myDataSource() {
		return new MyDataSource();
	}

	@Bean
	CustomerService customerService(MyDataSource dataSource) {
		return new CustomerService(dataSource);
	}
}


class MyDataSource {
}


class CustomerService {

	private final MyDataSource myDataSource;

	CustomerService(MyDataSource myDataSource) {
		this.myDataSource = myDataSource;
	}

	String greet(String name) {
		return ("Hello, " + name + "!");
	}
}

public class Application {

	public static void main(String[] args) {

		var annotationConfigApplicationContext =
			new AnnotationConfigApplicationContext(ApplicationWiringConfiguration.class);

		var cs = annotationConfigApplicationContext.getBean(CustomerService.class);
		System.out.println(cs.greet("Spring Framework"));

	}

}
