package com.eggtartc.airxbackend.repository;

import com.eggtartc.airxbackend.entity.File;
import com.eggtartc.airxbackend.entity.FileShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileShareRepository extends JpaRepository<FileShare, Integer> {
    Optional<FileShare> findByAlias(String alias);

    Page<FileShare> findAllByUser_Id(
        Pageable pageable,
        Integer userId
    );
}
