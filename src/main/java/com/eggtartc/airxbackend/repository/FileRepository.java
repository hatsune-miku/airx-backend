package com.eggtartc.airxbackend.repository;

import com.eggtartc.airxbackend.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {
    Page<File> findAllByUser_IdAndNameContains(
        Pageable pageable,
        Integer userId,
        String name
    );
}
