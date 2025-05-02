package com.coop.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor; // 기본 생성자 추가 (필요할 수 있음)
import lombok.AllArgsConstructor; // 모든 필드 생성자 추가 (필요할 수 있음)

@Getter
@Builder
@NoArgsConstructor      // 추가 (혹시 모를 상황 대비)
@AllArgsConstructor     // 추가 (혹시 모를 상황 대비)
@ToString
public class ProjectDTO {

    // 타입을 int 에서 Long 으로 변경
    private Long projectId;
    private String projectName;
    private LocalDateTime createDate;

    // @Builder를 사용하므로 생성자는 Lombok이 관리합니다.
    // 직접 생성자를 정의했다면 해당 생성자도 수정해야 합니다.
}