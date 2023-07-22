package com.eggtartc.airxbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "file_share", schema = "airx")
public class FileShare {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "visits", nullable = false)
    private Integer visits;
    @Basic
    @Column(name = "downloads", nullable = false)
    private Integer downloads;
    @Basic
    @Column(name = "alias", nullable = false, length = 255)
    private String alias;
    @ManyToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id", nullable = false)
    private File file;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileShare fileShare = (FileShare) o;
        return Objects.equals(id, fileShare.id) && Objects.equals(visits, fileShare.visits) && Objects.equals(downloads, fileShare.downloads) && Objects.equals(alias, fileShare.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, visits, downloads, alias);
    }
}
