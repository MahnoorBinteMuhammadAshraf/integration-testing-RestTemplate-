package com.advancedtaskmanager.repository;

import com.advancedtaskmanager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>
{
     Optional<Category> findByName(final String name);
}
