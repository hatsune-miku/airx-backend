package com.eggtartc.airxbackend.repository;

import com.eggtartc.airxbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUid(Integer uid);
}
