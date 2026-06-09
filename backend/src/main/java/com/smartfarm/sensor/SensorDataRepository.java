package com.smartfarm.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    @Query("SELECT s FROM SensorData s WHERE s.deviceId = :deviceId AND s.sensorType = :sensorType AND s.recordedAt >= :from ORDER BY s.recordedAt DESC")
    List<SensorData> findRecentByDeviceAndType(
        @Param("deviceId") String deviceId,
        @Param("sensorType") String sensorType,
        @Param("from") OffsetDateTime from
    );

    @Query("SELECT s FROM SensorData s WHERE s.recordedAt >= :from ORDER BY s.recordedAt ASC")
    List<SensorData> findAllRecent(@Param("from") OffsetDateTime from);
}
