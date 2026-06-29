package com.example.netnovel_server.audio.config;

import com.example.netnovel_server.audio.entity.AudioEncoding;
import com.example.netnovel_server.audio.entity.AudioProvider;
import com.example.netnovel_server.audio.entity.AudioStorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.audio")
public class AudioProperties {

    private AudioStorageType storageType = AudioStorageType.LOCAL;
    private String storagePath = "storage/audio";
    private AudioProvider defaultProvider = AudioProvider.AWS_POLLY;
    private String defaultLanguageCode = "en-US";
    private String defaultVoiceName = "Joanna";
    private String defaultEngine = "neural";
    private BigDecimal defaultSpeakingRate = new BigDecimal("1.00");
    private BigDecimal defaultPitch = new BigDecimal("0.00");
    private AudioEncoding defaultAudioEncoding = AudioEncoding.MP3;
    private int maxChunkCharacters = 2800;
    private int maxTotalCharacters = 50000;
    private long userMonthlyCharacterQuota = 100000;
    private long managerMonthlyCharacterQuota = 1000000;
    private long adminMonthlyCharacterQuota = -1;
    private long cacheTtlDays = 30;
    private String awsRegion = "ap-southeast-1";
    private String awsAccessKeyId = "";
    private String awsSecretAccessKey = "";

    public AudioStorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(AudioStorageType storageType) {
        this.storageType = storageType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public AudioProvider getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(AudioProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        this.defaultLanguageCode = defaultLanguageCode;
    }

    public String getDefaultVoiceName() {
        return defaultVoiceName;
    }

    public void setDefaultVoiceName(String defaultVoiceName) {
        this.defaultVoiceName = defaultVoiceName;
    }

    public String getDefaultEngine() {
        return defaultEngine;
    }

    public void setDefaultEngine(String defaultEngine) {
        this.defaultEngine = defaultEngine;
    }

    public BigDecimal getDefaultSpeakingRate() {
        return defaultSpeakingRate;
    }

    public void setDefaultSpeakingRate(BigDecimal defaultSpeakingRate) {
        this.defaultSpeakingRate = defaultSpeakingRate;
    }

    public BigDecimal getDefaultPitch() {
        return defaultPitch;
    }

    public void setDefaultPitch(BigDecimal defaultPitch) {
        this.defaultPitch = defaultPitch;
    }

    public AudioEncoding getDefaultAudioEncoding() {
        return defaultAudioEncoding;
    }

    public void setDefaultAudioEncoding(AudioEncoding defaultAudioEncoding) {
        this.defaultAudioEncoding = defaultAudioEncoding;
    }

    public int getMaxChunkCharacters() {
        return maxChunkCharacters;
    }

    public void setMaxChunkCharacters(int maxChunkCharacters) {
        this.maxChunkCharacters = maxChunkCharacters;
    }

    public int getMaxTotalCharacters() {
        return maxTotalCharacters;
    }

    public void setMaxTotalCharacters(int maxTotalCharacters) {
        this.maxTotalCharacters = maxTotalCharacters;
    }

    public long getUserMonthlyCharacterQuota() {
        return userMonthlyCharacterQuota;
    }

    public void setUserMonthlyCharacterQuota(long userMonthlyCharacterQuota) {
        this.userMonthlyCharacterQuota = userMonthlyCharacterQuota;
    }

    public long getManagerMonthlyCharacterQuota() {
        return managerMonthlyCharacterQuota;
    }

    public void setManagerMonthlyCharacterQuota(long managerMonthlyCharacterQuota) {
        this.managerMonthlyCharacterQuota = managerMonthlyCharacterQuota;
    }

    public long getAdminMonthlyCharacterQuota() {
        return adminMonthlyCharacterQuota;
    }

    public void setAdminMonthlyCharacterQuota(long adminMonthlyCharacterQuota) {
        this.adminMonthlyCharacterQuota = adminMonthlyCharacterQuota;
    }

    public long getCacheTtlDays() {
        return cacheTtlDays;
    }

    public void setCacheTtlDays(long cacheTtlDays) {
        this.cacheTtlDays = cacheTtlDays;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }
}
