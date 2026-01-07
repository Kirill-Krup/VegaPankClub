package com.actisys.userservice.dto.ReviewResponseDtos;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReviewDTO {
  private String reviewText;
  private int stars;
}
