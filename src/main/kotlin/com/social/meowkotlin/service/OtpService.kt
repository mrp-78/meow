package com.social.meowkotlin.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class OtpService(private val redisTemplate: RedisTemplate<String, String>, private val smsService: SmsService) {
    private val random = Random()

    @Value("\${otp.expiration}")
    private val otpExpiration: Long = 0

    fun generateAndSendOtp(phoneNumber: String) {
        val otp = String.format("%06d", random.nextInt(1000000))
        redisTemplate.opsForValue()[getOtpKey(phoneNumber), otp, otpExpiration] = TimeUnit.SECONDS
        smsService.sendSms(phoneNumber, "Your OTP is: $otp")
    }

    fun validateOtp(phoneNumber: String, otp: String?): Boolean {
        val storedOtp = redisTemplate.opsForValue()[getOtpKey(phoneNumber)]
        return otp != null && otp == storedOtp
    }

    private fun getOtpKey(phoneNumber: String): String {
        return "otp:$phoneNumber"
    }
}
