package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WecomMaterialRepository extends JpaRepository<WecomMaterial, Long> {

    @Query("SELECT m FROM WecomMaterial m WHERE " +
           "(:type IS NULL OR m.type = :type) AND " +
           "(:title IS NULL OR m.title LIKE CONCAT('%', :title, '%')) AND " +
           "(:sourceType IS NULL OR m.sourceType = :sourceType)")
    Page<WecomMaterial> findByFilters(
            @Param("type") String type,
            @Param("title") String title,
            @Param("sourceType") String sourceType,
            Pageable pageable);
}
