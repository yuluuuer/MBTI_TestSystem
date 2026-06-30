package com.mbti.config;

import com.mbti.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            log.info("No users found. New registered accounts will use USER role by default.");
            return;
        }

        if (userRepository.countByIsActiveTrue() == 0) {
            var users = userRepository.findAll();
            users.forEach(user -> user.setIsActive(true));
            userRepository.saveAll(users);
            log.info("Migrated {} existing users to active status.", users.size());
        }
    }
}
