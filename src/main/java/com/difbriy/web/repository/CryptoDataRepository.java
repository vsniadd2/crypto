package com.difbriy.web.repository;

import com.difbriy.web.entity.CryptoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CryptoDataRepository extends JpaRepository<CryptoData, Long> {

    List<CryptoData> findBySymbolOrderByTimestampDesc(String symbol);
    
    @Query("SELECT c FROM CryptoData c WHERE c.symbol = :symbol AND c.timestamp >= :startTime ORDER BY c.timestamp ASC")
    List<CryptoData> findBySymbolAndTimestampAfterOrderByTimestampAsc(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT c FROM CryptoData c WHERE c.symbol = :symbol AND c.timestamp >= :startTime ORDER BY c.timestamp ASC")
    List<CryptoData> findLast24HoursBySymbol(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT c FROM CryptoData c WHERE c.symbol = :symbol AND c.timestamp >= :startTime ORDER BY c.timestamp ASC")
    List<CryptoData> findLast7DaysBySymbol(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT c FROM CryptoData c WHERE c.symbol = :symbol AND c.timestamp >= :startTime ORDER BY c.timestamp ASC")
    List<CryptoData> findLast30DaysBySymbol(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT c FROM CryptoData c WHERE c.symbol = :symbol AND c.timestamp >= :startTime ORDER BY c.timestamp ASC")
    List<CryptoData> findLast6MonthsBySymbol(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT c FROM CryptoData c WHERE c.timestamp = (SELECT MAX(c2.timestamp) FROM CryptoData c2 WHERE c2.symbol = c.symbol) ORDER BY c.rank ASC")
    List<CryptoData> findLatestDataForAllCryptos();

    @Query("SELECT c FROM CryptoData c WHERE c.symbol = :symbol ORDER BY c.timestamp DESC LIMIT 1")
    CryptoData findLatestBySymbol(@Param("symbol") String symbol);

    @Modifying
    @Transactional
    @Query("DELETE FROM CryptoData c WHERE c.timestamp < :cutoffDate")
    void deleteOldData(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT DISTINCT c.symbol FROM CryptoData c ORDER BY c.symbol")
    List<String> findAllSymbols();
} 