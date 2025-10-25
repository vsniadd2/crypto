package com.difbriy.web.repository;

import com.difbriy.web.entity.CryptoPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoPredictionRepository extends JpaRepository<CryptoPrediction, Long> {
    
    // Найти последний прогноз для конкретной криптовалюты
    @Query("SELECT p FROM CryptoPrediction p WHERE p.symbol = :symbol ORDER BY p.createdAt DESC LIMIT 1")
    Optional<CryptoPrediction> findLatestBySymbol(@Param("symbol") String symbol);
    
    // Найти все прогнозы для конкретной криптовалюты
    List<CryptoPrediction> findBySymbolOrderByCreatedAtDesc(String symbol);
    
    // Найти прогнозы за последние N часов
    @Query("SELECT p FROM CryptoPrediction p WHERE p.symbol = :symbol AND p.createdAt >= :startTime ORDER BY p.createdAt ASC")
    List<CryptoPrediction> findBySymbolAndCreatedAtAfterOrderByCreatedAtAsc(@Param("symbol") String symbol, @Param("startTime") LocalDateTime startTime);
    
    // Найти прогнозы с высокой уверенностью
    @Query("SELECT p FROM CryptoPrediction p WHERE p.confidenceScore >= :minConfidence ORDER BY p.createdAt DESC")
    List<CryptoPrediction> findByHighConfidence(@Param("minConfidence") BigDecimal minConfidence);
    
    // Найти последние прогнозы для всех криптовалют
    @Query("SELECT p FROM CryptoPrediction p WHERE p.createdAt = (SELECT MAX(p2.createdAt) FROM CryptoPrediction p2 WHERE p2.symbol = p.symbol) ORDER BY p.createdAt DESC")
    List<CryptoPrediction> findLatestPredictionsForAllCryptos();
    
    // Удалить старые прогнозы (старше N дней)
    @Modifying
    @Transactional
    @Query("DELETE FROM CryptoPrediction p WHERE p.createdAt < :cutoffDate")
    void deleteOldPredictions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
