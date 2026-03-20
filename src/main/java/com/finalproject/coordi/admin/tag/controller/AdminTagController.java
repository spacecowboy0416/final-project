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

    // 태그 관리 UI에서 분류(상의, 하의 등) 드롭다운을 동적으로 생성하기 위해, DB에 저장된 모든 태그 타입을 조회함.
    @GetMapping("/types")
    public ResponseEntity<List<String>> getTagTypes() {
        return ResponseEntity.ok(adminTagService.getTagTypes());
    }

    // 관리자가 특정 분류에 속한 태그 목록을 보고 관리할 수 있도록, 타입별 태그를 조회함.
    @GetMapping
    public ResponseEntity<List<TagDto>> getTagsByType(@RequestParam("type") String type) {
        return ResponseEntity.ok(adminTagService.getTagsByType(type));
    }

    // 관리자가 새로운 추천 기준(태그)을 시스템에 추가할 수 있도록, 태그 생성 기능을 제공함.
    @PostMapping
    public ResponseEntity<TagDto> addTag(@RequestBody TagDto tag) {
        TagDto newTag = adminTagService.addTag(tag);
        return ResponseEntity.created(URI.create("/api/admin/tags/" + newTag.getTagId())).body(newTag);
    }

    // 오타 수정 등 기존 태그의 이름을 변경해야 할 경우를 대비해, 태그 수정 기능을 제공함.
    @PutMapping("/{tagId}")
    public ResponseEntity<Void> updateTag(@PathVariable Long tagId, @RequestBody Map<String, String> payload) {
        adminTagService.updateTag(tagId, payload.get("name"));
        return ResponseEntity.noContent().build();
    }

    // 더 이상 사용하지 않거나 잘못 생성된 태그를 시스템에서 제거하기 위해, 태그 삭제 기능을 제공함.
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        adminTagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
