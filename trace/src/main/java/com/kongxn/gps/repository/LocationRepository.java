package com.kongxn.gps.repository;

import com.kongxn.gps.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<LocationEntity, Integer> {

    @Query(nativeQuery = true, value = "select * from location_log where platform = :platform and username = :username  order by query_time desc limit 1")
    public LocationEntity getLastLocation(@Param("platform") String platform, @Param("username") String username);
}
