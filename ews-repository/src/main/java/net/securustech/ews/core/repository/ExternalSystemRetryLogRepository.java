/*
 * Copyright (c) 2012 SECURUSTECH and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADERS
 */
package net.securustech.ews.core.repository;

import net.securustech.ews.core.repository.entity.ExternalSystemRetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalSystemRetryLogRepository extends JpaRepository<ExternalSystemRetryLog, Long> {

    @Query("SELECT esrl FROM ExternalSystemRetryLog esrl WHERE esrl.status IN (:status)")
    Iterable<ExternalSystemRetryLog> findByStatus(@Param("status") List<String> status);

    @Query("SELECT esrl FROM ExternalSystemRetryLog esrl WHERE esrl.status IN (:status) AND esrl.retryCount <= :retryCount")
    Iterable<ExternalSystemRetryLog> findByStatusAndRetryCount(@Param("status") List<String> status, @Param("retryCount") Short retryCount);

    @Query("SELECT esrl FROM ExternalSystemRetryLog esrl WHERE esrl.status IN (:status) AND esrl.retryCount <= :retryCount AND esrl.createdBy = :createdBy")
    Iterable<ExternalSystemRetryLog> findByStatusAndRetryCountAndUser(@Param("status") List<String> status, @Param("retryCount") Short retryCount, @Param("createdBy") String createdBy);
}
