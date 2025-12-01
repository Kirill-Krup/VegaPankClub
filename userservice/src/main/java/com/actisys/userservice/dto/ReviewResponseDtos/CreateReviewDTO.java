package com.actisys.userservice.dto.ReviewResponseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateReviewDTO {
  private final String reviewText;
  private final int stars;
}
