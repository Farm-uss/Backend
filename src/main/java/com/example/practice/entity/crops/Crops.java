package com.example.practice.entity.crops;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crops")
@Data @NoArgsConstructor
public class Crops {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cropsId;
    private String name;
    @Column(name = "farm_id")
    private Long farmId;
}