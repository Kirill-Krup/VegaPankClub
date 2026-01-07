package com.actisys.userservice.dto.ReviewResponseDtos;

import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ReviewDTO {
  private Long id;
  private UserSimpleProfileDTO user;
  private String reviewText;
  private LocalDateTime createdAt;
  private int stars;

  private boolean visible;
}
