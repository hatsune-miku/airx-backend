package com.eggtartc.airxbackend.helper;

import com.eggtartc.airxbackend.enums.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class RedisHelper<Tk, Tv> {
    RedisTemplate<Tk, Tv> redis;

    public RedisHelper(RedisTemplate<Tk, Tv> redis) {
        this.redis = redis;
    }

    public void set(Tk key, Tv value) {
        redis.opsForValue().set(key, value);
    }

    public Tv get(Tk key, Tv def) {
        Tv ret = redis.opsForValue().get(key);
        return ret == null ? def : ret;
    }

    public void delete(Tk key) {
        redis.delete(key);
    }

    public Boolean hasKey(Tk key) {
        return redis.hasKey(key);
    }

    public void expire(Tk key, long timeoutMillis) {
        redis.expire(key, Duration.ofMillis(timeoutMillis));
    }

    public void persist(Tk key) {
        redis.persist(key);
    }

    public void rename(Tk oldKey, Tk newKey) {
        redis.rename(oldKey, newKey);
    }
}
