package com.finalproject.coordi.admin.tag.controller;

import com.finalproject.coordi.admin.tag.dto.TagDto;
import com.finalproject.coordi.admin.tag.service.AdminTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminTagService adminTagService;

    @GetMapping("/types")
    public ResponseEntity<List<String>> getTagTypes() {
        return ResponseEntity.ok(adminTagService.getTagTypes());
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> getTagsByType(@RequestParam("type") String type) {
        return ResponseEntity.ok(adminTagService.getTagsByType(type));
    }

    @PostMapping
    public ResponseEntity<TagDto> addTag(@RequestBody TagDto tag) {
        TagDto newTag = adminTagService.addTag(tag);
        return ResponseEntity.created(URI.create("/api/admin/tags/" + newTag.getTagId())).body(newTag);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<Void> updateTag(@PathVariable Long tagId, @RequestBody Map<String, String> payload) {
        adminTagService.updateTag(tagId, payload.get("name"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        adminTagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
