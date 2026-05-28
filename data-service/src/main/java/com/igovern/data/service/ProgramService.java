package com.igovern.data.service;

import com.igovern.data.dto.ProgramDtos.ParticipantRequest;
import com.igovern.data.dto.ProgramDtos.ProgramRequest;
import com.igovern.data.entity.Participant;
import com.igovern.data.entity.ResearchProgram;
import com.igovern.data.repository.ParticipantRepository;
import com.igovern.data.repository.ResearchProgramRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class ProgramService {

    private static final Logger log = LogManager.getLogger(ProgramService.class);

    private final ResearchProgramRepository programs;
    private final ParticipantRepository participants;
    private final AmqClient amqClient;

    public ProgramService(ResearchProgramRepository programs,
                          ParticipantRepository participants,
                          AmqClient amqClient) {
        this.programs = programs;
        this.participants = participants;
        this.amqClient = amqClient;
    }

    @Transactional(readOnly = true)
    public List<ResearchProgram> findAll() {
        return programs.findAll();
    }

    @Transactional(readOnly = true)
    public ResearchProgram findById(Long id) {
        return programs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "program not found"));
    }

    public ResearchProgram create(ProgramRequest req) {
        ResearchProgram p = new ResearchProgram();
        apply(p, req);
        ResearchProgram saved = programs.save(p);
        log.info("Created program id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    public ResearchProgram update(Long id, ProgramRequest req) {
        ResearchProgram p = findById(id);
        apply(p, req);
        log.info("Updated program id={}", id);
        return p;
    }

    public void delete(Long id) {
        ResearchProgram p = findById(id);
        programs.delete(p);
        log.info("Deleted program id={}", id);
        amqClient.notifyDeletion("ResearchProgram", id);
    }

    // ---- Participants (child) ----

    @Transactional(readOnly = true)
    public List<Participant> listParticipants(Long programId) {
        findById(programId); // verify parent exists
        return participants.findByProgramId(programId);
    }

    public Participant addParticipant(Long programId, ParticipantRequest req) {
        ResearchProgram parent = findById(programId);
        Participant pt = new Participant();
        apply(pt, req);
        pt.setProgram(parent);
        Participant saved = participants.save(pt);
        log.info("Added participant id={} to program id={}", saved.getId(), programId);
        return saved;
    }

    public Participant updateParticipant(Long participantId, ParticipantRequest req) {
        Participant pt = participants.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "participant not found"));
        apply(pt, req);
        log.info("Updated participant id={}", participantId);
        return pt;
    }

    public void deleteParticipant(Long participantId) {
        Participant pt = participants.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "participant not found"));
        participants.delete(pt);
        log.info("Deleted participant id={}", participantId);
        amqClient.notifyDeletion("Participant", participantId);
    }

    private void apply(ResearchProgram p, ProgramRequest req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setBudget(req.getBudget());
    }

    private void apply(Participant pt, ParticipantRequest req) {
        pt.setFirstName(req.getFirstName());
        pt.setLastName(req.getLastName());
        pt.setDateOfBirth(req.getDateOfBirth());
        pt.setEnrollmentDate(req.getEnrollmentDate());
        pt.setWeightKg(req.getWeightKg());
    }
}
