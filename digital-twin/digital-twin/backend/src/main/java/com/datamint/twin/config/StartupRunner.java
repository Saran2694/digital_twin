package com.datamint.twin.config;

import com.datamint.twin.model.AppUser;
import com.datamint.twin.repository.UserRepository;
import com.datamint.twin.service.DataImportService;
import com.datamint.twin.service.MaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.io.InputStream;

/** First start: creates the admin user and imports the Excel database. */
@Component
public class StartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final DataImportService importer;
    private final MaintenanceService maintenance;
    private final JdbcTemplate jdbc;
    private final ResourceLoader loader;
    @Value("${twin.admin.username}") private String adminUser;
    @Value("${twin.admin.password}") private String adminPass;
    @Value("${twin.admin.full-name}") private String adminName;
    @Value("${twin.import.on-startup:true}") private boolean importOnStartup;
    @Value("${twin.import.file}") private String importFile;

    public StartupRunner(UserRepository users, PasswordEncoder encoder, DataImportService importer,
                         MaintenanceService maintenance, JdbcTemplate jdbc, ResourceLoader loader) {
        this.users = users; this.encoder = encoder; this.importer = importer;
        this.maintenance = maintenance; this.jdbc = jdbc; this.loader = loader;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.findByUsername(adminUser).isEmpty()) {
            AppUser u = new AppUser();
            u.setUsername(adminUser);
            u.setPasswordHash(encoder.encode(adminPass));
            u.setFullName(adminName);
            u.setRole("ADMIN");
            users.save(u);
            log.info("Admin user '{}' created", adminUser);
        }
        Long count = jdbc.queryForObject("select count(*) from component_inspections", Long.class);
        if (importOnStartup && count != null && count == 0) {
            try {
                Resource res = loader.getResource(importFile);
                try (InputStream in = res.getInputStream()) {
                    log.info("Import finished: {}", importer.importWorkbook(in));
                }
                log.info("Maintenance requests created: {}", maintenance.autoGenerate());
            } catch (Exception e) {
                log.error("Excel import failed - use Settings > Import in the admin dashboard", e);
            }
        }
    }
}
