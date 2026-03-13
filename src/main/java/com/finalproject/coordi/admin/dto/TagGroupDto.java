package com.finalproject.coordi.admin.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TagGroupDto {
    private String type;
    private List<TagDto> tags;
}
