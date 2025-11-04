package com.social.meow.cache;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvictRange {
    String cacheName();
    String startKey();
    String pageSize();
}
