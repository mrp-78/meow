package com.social.meow.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void sendSms(String phoneNumber, String message) {
        // Call the third-party SMS provider's API
        System.out.printf("Sending SMS to %s: %s%n", phoneNumber, message);
        // Example: Use RestTemplate to send HTTP requests to the SMS API
    }
}
