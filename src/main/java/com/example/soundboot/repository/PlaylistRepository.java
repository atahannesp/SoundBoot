package com.example.soundboot.repository;

import com.example.soundboot.entity.PlaylistEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistRepository extends CrudRepository<PlaylistEntity, UUID> {
}