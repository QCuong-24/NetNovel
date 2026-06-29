package com.example.netnovel_server.audio.service;

import com.example.netnovel_server.audio.config.AudioProperties;
import com.example.netnovel_server.audio.entity.AudioUsageCounter;
import com.example.netnovel_server.audio.entity.AudioUsagePeriodType;
import com.example.netnovel_server.audio.repository.AudioUsageCounterRepository;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.exception.TooManyRequestsException;
import com.example.netnovel_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Service
public class AudioQuotaService {

    private static final long UNLIMITED = -1;

    private final AudioUsageCounterRepository usageCounterRepository;
    private final UserRepository userRepository;
    private final AudioProperties properties;

    public AudioQuotaService(
        AudioUsageCounterRepository usageCounterRepository,
        UserRepository userRepository,
        AudioProperties properties
    ) {
        this.usageCounterRepository = usageCounterRepository;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Transactional
    public void reserveMonthlyCharacters(Long userId, int requestedCharacters) {
        if (userId == null) {
            return;
        }

        User user = findUser(userId);
        long limit = resolveMonthlyLimit(user);
        if (limit == UNLIMITED) {
            return;
        }

        AudioUsageCounter counter = getOrCreateMonthlyCounter(user);
        long used = counter.getRequestedCharacters() == null ? 0L : counter.getRequestedCharacters();
        if (used + requestedCharacters > limit) {
            long remaining = Math.max(0L, limit - used);
            throw new TooManyRequestsException(
                "Audio character quota exceeded. Remaining monthly characters: " + remaining
            );
        }

        counter.setRequestedCharacters(used + requestedCharacters);
        usageCounterRepository.save(counter);
    }

    @Transactional
    public void recordGeneratedCharacters(Long userId, int generatedCharacters) {
        if (userId == null) {
            return;
        }

        User user = findUser(userId);
        AudioUsageCounter counter = getOrCreateMonthlyCounter(user);
        counter.setGeneratedCharacters((counter.getGeneratedCharacters() == null ? 0L : counter.getGeneratedCharacters()) + generatedCharacters);
        counter.setGenerationCount((counter.getGenerationCount() == null ? 0L : counter.getGenerationCount()) + 1);
        usageCounterRepository.save(counter);
    }

    @Transactional
    public void releaseReservedCharacters(Long userId, int reservedCharacters) {
        if (userId == null) {
            return;
        }

        User user = findUser(userId);
        AudioUsageCounter counter = getOrCreateMonthlyCounter(user);
        long requestedCharacters = counter.getRequestedCharacters() == null ? 0L : counter.getRequestedCharacters();
        counter.setRequestedCharacters(Math.max(0L, requestedCharacters - reservedCharacters));
        usageCounterRepository.save(counter);
    }

    @Transactional
    public void recordCacheHit(Long userId) {
        if (userId == null) {
            return;
        }

        User user = findUser(userId);
        AudioUsageCounter counter = getOrCreateMonthlyCounter(user);
        counter.setCacheHitCount((counter.getCacheHitCount() == null ? 0L : counter.getCacheHitCount()) + 1);
        usageCounterRepository.save(counter);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AudioUsageCounter getOrCreateMonthlyCounter(User user) {
        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        return usageCounterRepository
            .findByUser_IdAndPeriodTypeAndPeriodStart(user.getId(), AudioUsagePeriodType.MONTHLY, periodStart)
            .orElseGet(() -> AudioUsageCounter.builder()
                .user(user)
                .periodType(AudioUsagePeriodType.MONTHLY)
                .periodStart(periodStart)
                .build());
    }

    private long resolveMonthlyLimit(User user) {
        Set<Role> roles = user.getRoles();
        if (roles != null && roles.contains(Role.ADMIN)) {
            return properties.getAdminMonthlyCharacterQuota();
        }
        if (roles != null && roles.contains(Role.MANAGER)) {
            return properties.getManagerMonthlyCharacterQuota();
        }

        return properties.getUserMonthlyCharacterQuota();
    }
}
