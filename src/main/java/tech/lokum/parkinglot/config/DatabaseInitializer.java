package tech.lokum.parkinglot.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {

        String sql = """
                CREATE UNIQUE INDEX IF NOT EXISTS
                uk_parking_lot_active_name_address
                ON parking_lots (name, address)
                WHERE status = 'ACTIVE'
                """;

        jdbcTemplate.execute(sql);
    }
}
