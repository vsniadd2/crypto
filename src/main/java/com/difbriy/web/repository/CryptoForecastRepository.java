package com.difbriy.web.repository;

import com.difbriy.web.entity.CryptoForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoForecastRepository extends JpaRepository<CryptoForecast, Long> {
    
    List<CryptoForecast> findByAnalysisIdOrderByForecastPeriod(Long analysisId);
    
    @Query("SELECT cf FROM CryptoForecast cf WHERE cf.analysis.symbol = :symbol AND cf.forecastPeriod = :period ORDER BY cf.forecastDate DESC")
    List<CryptoForecast> findBySymbolAndPeriod(@Param("symbol") String symbol, @Param("period") String period);
    
    @Query("SELECT cf FROM CryptoForecast cf WHERE cf.analysis.symbol = :symbol ORDER BY cf.forecastDate DESC")
    List<CryptoForecast> findBySymbolOrderByForecastDateDesc(@Param("symbol") String symbol);
}
