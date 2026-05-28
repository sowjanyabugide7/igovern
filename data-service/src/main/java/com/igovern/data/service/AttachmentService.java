package com.igovern.data.service;

import com.igovern.data.entity.Attachment;
import com.igovern.data.entity.ResearchProgram;
import com.igovern.data.repository.AttachmentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class AttachmentService {

    private static final Logger log = LogManager.getLogger(AttachmentService.class);

    private final AttachmentRepository attachments;
    private final ProgramService programService;
    private final AmqClient amqClient;
    private final Path uploadDir;
    private final Set<String> allowedTypes;

    public AttachmentService(AttachmentRepository attachments,
                             ProgramService programService,
                             AmqClient amqClient,
                             @Value("${app.upload.dir}") String uploadDir,
                             @Value("${app.upload.allowed-content-types}") String allowedTypes) throws IOException {
        this.attachments = attachments;
        this.programService = programService;
        this.amqClient = amqClient;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedTypes = Set.of(allowedTypes.split(","));
        Files.createDirectories(this.uploadDir);
    }

    public Attachment upload(Long programId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "file is required");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE,
                    "content type not allowed: " + file.getContentType());
        }

        ResearchProgram program = programService.findById(programId);

        String original = sanitize(file.getOriginalFilename());
        String stored = UUID.randomUUID() + "_" + original;
        Path target = uploadDir.resolve(stored).normalize();

        // Defense in depth: prevent path traversal
        if (!target.startsWith(uploadDir)) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid file path");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        Attachment att = new Attachment();
        att.setOriginalName(original);
        att.setStoredName(stored);
        att.setContentType(file.getContentType());
        att.setSize(file.getSize());
        att.setProgram(program);
        Attachment saved = attachments.save(att);
        log.info("Stored attachment id={} ({} bytes) for program id={}",
                saved.getId(), saved.getSize(), programId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Attachment> listForProgram(Long programId) {
        programService.findById(programId);
        return attachments.findByProgramId(programId);
    }

    @Transactional(readOnly = true)
    public Attachment get(Long id) {
        return attachments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "attachment not found"));
    }

    public Path resolveStoredPath(Attachment att) {
        Path p = uploadDir.resolve(att.getStoredName()).normalize();
        if (!p.startsWith(uploadDir)) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid path");
        }
        return p;
    }

    public void delete(Long id) throws IOException {
        Attachment att = get(id);
        Files.deleteIfExists(resolveStoredPath(att));
        attachments.delete(att);
        log.info("Deleted attachment id={}", id);
        amqClient.notifyDeletion("Attachment", id);
    }

    private static String sanitize(String name) {
        if (name == null) return "file";
        String cleaned = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (cleaned.length() > 100) cleaned = cleaned.substring(cleaned.length() - 100);
        return cleaned;
    }
}
