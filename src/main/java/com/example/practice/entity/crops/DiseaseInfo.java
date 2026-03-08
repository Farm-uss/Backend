package com.example.practice.entity.crops;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "disease_info")
public class DiseaseInfo {

    @Id
    @Column(name = "disease_id", length = 50)
    private String diseaseId;

    @Column(name = "disease_name", nullable = false, length = 120)
    private String diseaseName;

    @Column(name = "disease_description", columnDefinition = "text")
    private String diseaseDescription;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "diseaseInfo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiseaseGuide> guides = new ArrayList<>();
}
