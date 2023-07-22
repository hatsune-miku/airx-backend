package com.eggtartc.airxbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "file_store", schema = "airx")
public class FileStore {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @JsonIgnore
    @Column(name = "absolute_path", nullable = false, length = -1)
    private String absolutePath;
    @Basic
    @JsonIgnore
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    @Basic
    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;
    @Basic
    @Column(name = "size", nullable = false)
    private Long size;
    @Basic
    @Column(name = "uploaded_at", nullable = false)
    private Timestamp uploadedAt;

    @OneToMany(mappedBy = "fileStore")
    @JsonIgnore
    private Collection<File> files;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileStore fileStore = (FileStore) o;
        return Objects.equals(id, fileStore.id) && Objects.equals(absolutePath, fileStore.absolutePath) && Objects.equals(sha256, fileStore.sha256);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, absolutePath, sha256);
    }
}
