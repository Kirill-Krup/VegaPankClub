package com.actisys.userservice.service;

import com.actisys.userservice.dto.ReviewResponseDtos.CreateReviewDTO;
import com.actisys.userservice.dto.ReviewResponseDtos.ReviewDTO;
import java.util.List;

public interface ReviewService {

  List<ReviewDTO> getAllReviews();

  ReviewDTO addReview(CreateReviewDTO createReviewDTO, Long userId);

  void editVisibility(Long reviewId);

  void updateReview(Long reviewId, CreateReviewDTO updateReviewDTO);
}
