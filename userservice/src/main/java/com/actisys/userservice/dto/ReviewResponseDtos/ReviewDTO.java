package com.actisys.userservice.dto.ReviewResponseDtos;

import com.actisys.userservice.dto.UserResponseDtos.UserSimpleProfileDTO;
import java.time.LocalDateTime;
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
  private final int reviewId;
  private final UserSimpleProfileDTO user;
  private final String reviewText;
  private final LocalDateTime createdAt;
  private final int stars;
  private final boolean isVisible;
}
