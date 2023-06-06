package com.eggtartc.airxbackend.entity;

import com.eggtartc.airxbackend.util.HashUtil;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "uid", nullable = false)
    private int uid;
    @Basic
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Basic
    @Column(name = "valid_before", nullable = false)
    private Timestamp validBefore;
    @Basic
    @Column(name = "password", nullable = false, length = 128)
    private String password;
    @Basic
    @Column(name = "salt", nullable = false, length = 64)
    private String salt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() == user.getId();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String calculateSalt(String secret) {
        return HashUtil.sha256(secret + id + uid + name + validBefore.getTime() + secret + password);
    }

    public boolean isSaltValid(String secret) {
        return salt.equals(calculateSalt(secret));
    }

    public void correctSalt(String secret) {
        salt = calculateSalt(secret);
    }
}
