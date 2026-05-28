package com.igovern.data.controller;

import com.igovern.data.dto.ProgramDtos.ParticipantRequest;
import com.igovern.data.dto.ProgramDtos.ProgramRequest;
import com.igovern.data.entity.Participant;
import com.igovern.data.entity.ResearchProgram;
import com.igovern.data.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public List<ResearchProgram> list() {
        return programService.findAll();
    }

    @GetMapping("/{id}")
    public ResearchProgram get(@PathVariable Long id) {
        return programService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ResearchProgram> create(@Valid @RequestBody ProgramRequest req) {
        return ResponseEntity.ok(programService.create(req));
    }

    @PutMapping("/{id}")
    public ResearchProgram update(@PathVariable Long id, @Valid @RequestBody ProgramRequest req) {
        return programService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- participants ----

    @GetMapping("/{programId}/participants")
    public List<Participant> participants(@PathVariable Long programId) {
        return programService.listParticipants(programId);
    }

    @PostMapping("/{programId}/participants")
    public ResponseEntity<Participant> addParticipant(@PathVariable Long programId,
                                                      @Valid @RequestBody ParticipantRequest req) {
        return ResponseEntity.ok(programService.addParticipant(programId, req));
    }

    @PutMapping("/participants/{participantId}")
    public Participant updateParticipant(@PathVariable Long participantId,
                                         @Valid @RequestBody ParticipantRequest req) {
        return programService.updateParticipant(participantId, req);
    }

    @DeleteMapping("/participants/{participantId}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long participantId) {
        programService.deleteParticipant(participantId);
        return ResponseEntity.noContent().build();
    }
}
