package com.example.practice.entity.crops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "disease_guide")
public class DiseaseGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guide_id")
    private Long guideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private DiseaseInfo diseaseInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "guide_type", nullable = false, length = 20)
    private DiseaseGuideType guideType;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
