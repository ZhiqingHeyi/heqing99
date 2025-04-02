package com.hotel.repository;

import com.hotel.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    Optional<Statistics> findByDateAndType(LocalDate date, Statistics.StatisticsType type);

    List<Statistics> findByType(Statistics.StatisticsType type);

    @Query("SELECT s FROM Statistics s WHERE s.date >= ?1 AND s.date <= ?2 AND s.type = ?3")
    List<Statistics> findByDateRangeAndType(LocalDate startDate, LocalDate endDate, Statistics.StatisticsType type);

    @Query("SELECT AVG(s.occupancyRate) FROM Statistics s WHERE s.date >= ?1 AND s.date <= ?2 AND s.type = 'DAILY'")
    Double calculateAverageOccupancyRate(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.dailyRevenue) FROM Statistics s WHERE s.date >= ?1 AND s.date <= ?2")
    Double calculateTotalRevenue(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Statistics s WHERE s.date = ?1 AND s.type = 'DAILY'")
    Optional<Statistics> findDailyStatistics(LocalDate date);

    @Query("SELECT AVG(s.roomsCleaned) FROM Statistics s WHERE s.date >= ?1 AND s.date <= ?2 AND s.type = 'DAILY'")
    Double calculateAverageRoomsCleaned(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Statistics s WHERE s.type = ?1 ORDER BY s.date DESC")
    List<Statistics> findLatestStatistics(Statistics.StatisticsType type);

    @Query("SELECT s FROM Statistics s WHERE s.date >= ?1 AND s.type = 'MONTHLY'")
    List<Statistics> findMonthlyStatisticsSince(LocalDate startDate);
}