package com.example.practice.repository.farm;

import com.example.practice.entity.farm.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    @Query("SELECT fm.farm FROM FarmMember fm WHERE fm.userId = :userId")
    List<Farm> findMyFarmsByUserId(@Param("userId") Long userId);



}
