package com.igovern.data.repository;

import com.igovern.data.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByProgramId(Long programId);
}
