package it.samfrafx.tradingjournal.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "it.samfrafx.tradingjournal")
@EnableJpaRepositories(basePackages = "it.samfrafx.tradingjournal.datamodel.repository")
@EntityScan(basePackages = "it.samfrafx.tradingjournal.datamodel.data")
public class TradingjournalWebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingjournalWebappApplication.class, args);
    }
}
