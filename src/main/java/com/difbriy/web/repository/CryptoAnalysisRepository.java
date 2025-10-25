package com.difbriy.web.repository;

import com.difbriy.web.entity.CryptoAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoAnalysisRepository extends JpaRepository<CryptoAnalysis, Long> {
    
    List<CryptoAnalysis> findBySymbolOrderByAnalysisDateDesc(String symbol);
    
    Optional<CryptoAnalysis> findFirstBySymbolAndAnalysisTypeOrderByAnalysisDateDesc(String symbol, String analysisType);
    
    @Query("SELECT ca FROM CryptoAnalysis ca WHERE ca.symbol = :symbol AND ca.analysisDate >= :startDate ORDER BY ca.analysisDate DESC")
    List<CryptoAnalysis> findBySymbolAndAnalysisDateAfter(@Param("symbol") String symbol, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ca FROM CryptoAnalysis ca WHERE ca.analysisType = :analysisType ORDER BY ca.analysisDate DESC")
    List<CryptoAnalysis> findByAnalysisTypeOrderByAnalysisDateDesc(@Param("analysisType") String analysisType);
    
    @Query("SELECT DISTINCT ca.symbol FROM CryptoAnalysis ca ORDER BY ca.symbol")
    List<String> findDistinctSymbols();
}
