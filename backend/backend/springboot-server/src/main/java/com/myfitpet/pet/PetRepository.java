package com.myfitpet.pet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<PetEntity, Long> {
	java.util.Optional<PetEntity> findByOwnerId(Long ownerId);
}
