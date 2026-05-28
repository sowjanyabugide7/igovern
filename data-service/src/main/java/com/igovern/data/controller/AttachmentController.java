package com.igovern.data.controller;

import com.igovern.data.entity.Attachment;
import com.igovern.data.service.AttachmentService;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/programs/{programId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Attachment upload(@PathVariable Long programId,
                             @RequestPart("file") MultipartFile file) throws IOException {
        return attachmentService.upload(programId, file);
    }

    @GetMapping("/programs/{programId}")
    public List<Attachment> list(@PathVariable Long programId) {
        return attachmentService.listForProgram(programId);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment att = attachmentService.get(id);
        Resource resource = new PathResource(attachmentService.resolveStoredPath(att));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + att.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(att.getContentType()))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
