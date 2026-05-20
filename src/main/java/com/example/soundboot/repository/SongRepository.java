package com.example.soundboot.repository;

import com.example.soundboot.entity.SongEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SongRepository extends CrudRepository<SongEntity, UUID> {
}
