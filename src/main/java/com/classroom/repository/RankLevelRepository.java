package com.classroom.repository;

import com.classroom.entity.RankLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankLevelRepository extends JpaRepository<RankLevel, Long> {
    Optional<RankLevel> findByMinPointsLessThanEqualAndMaxPointsGreaterThanEqual(Integer minPoints, Integer maxPoints);
    Optional<RankLevel> findByName(String name);
}
