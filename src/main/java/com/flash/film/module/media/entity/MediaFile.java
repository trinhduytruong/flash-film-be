package com.flash.film.module.media.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.MediaType;
import com.flash.film.common.enums.StorageDisk;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "media_files", indexes = {
        @Index(name = "idx_media_user", columnList = "user_id"),
        @Index(name = "idx_media_checksum", columnList = "checksum_sha256")
})
public class MediaFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 30, nullable = false)
    private MediaType fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_disk", length = 30, nullable = false)
    private StorageDisk storageDisk;

    @Column(name = "storage_path", length = 500, nullable = false, unique = true)
    private String storagePath;

    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;

    @Column(name = "mime_type", length = 100, nullable = false)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "dpi")
    private Integer dpi;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, DELETED
}
