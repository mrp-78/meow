package com.social.meow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SmsService smsService;
    private final Random random = new Random();

    @Value("${otp.expiration}")
    private long otpExpiration;

    public OtpService(RedisTemplate<String, String> redisTemplate, SmsService smsService) {
        this.redisTemplate = redisTemplate;
        this.smsService = smsService;
    }

    public void generateAndSendOtp(String phoneNumber) {
        String otp = String.format("%06d", random.nextInt(1000000));
        redisTemplate.opsForValue().set(getOtpKey(phoneNumber), otp, otpExpiration, TimeUnit.SECONDS);
        smsService.sendSms(phoneNumber, "Your OTP is: " + otp);
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(getOtpKey(phoneNumber));
        return otp != null && otp.equals(storedOtp);
    }

    private String getOtpKey(String phoneNumber) {
        return "otp:" + phoneNumber;
    }
}
