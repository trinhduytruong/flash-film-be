package com.flash.film.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flash.film.common.enums.AppCode;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    private final boolean success;
    private final int httpCode;
    private final Integer code;
    private final String message;
    private final List<T> data;
    private final long totalElements;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final String timestamp;

    public PageResponse(Page<T> page, AppCode appCode, String message) {
        this.success = true;
        this.httpCode = HttpStatus.OK.value();
        this.code = appCode.getCode();
        this.message = message;
        this.data = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
