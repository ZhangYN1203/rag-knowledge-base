package com.example.app.repository;

import com.example.app.entity.ConfigProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigPropertyRepository extends JpaRepository<ConfigProperty, String> {
    List<ConfigProperty> findByConfigKeyStartingWith(String prefix);
}