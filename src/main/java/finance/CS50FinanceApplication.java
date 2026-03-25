package finance;

import finance.database.DatabaseSetup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CS50FinanceApplication {
    public static void main(String[] args) {
        DatabaseSetup.initialise();
        SpringApplication.run(CS50FinanceApplication.class, args);
    }
}
