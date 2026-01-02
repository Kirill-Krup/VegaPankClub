package com.actisys.userservice.service;

import com.actisys.userservice.dto.ReviewResponseDtos.CreateReviewDTO;
import com.actisys.userservice.dto.ReviewResponseDtos.ReviewDTO;
import java.util.List;

public interface ReviewService {

  /**
   * Retrieves all reviews.
   * Fetches all review records from repository and converts them to DTOs.
   *
   * @return list of all reviews in ReviewDTO format
   */
  List<ReviewDTO> getAllReviews();

  /**
   * Creates a new review for specified user.
   * Finds user by ID, creates review with provided stars and text,
   * saves to database and returns created review DTO.
   *
   * @param createReviewDTO review creation data (stars, text)
   * @param userId ID of the user creating the review
   * @return DTO of the created review
   */
  ReviewDTO addReview(CreateReviewDTO createReviewDTO, Long userId);

  /**
   * Toggles review visibility.
   * Finds review by ID and switches its visibility status to opposite,
   * then saves changes to database.
   *
   * @param reviewId ID of review to toggle visibility
   */
  void editVisibility(Long reviewId);

  /**
   * Updates existing review.
   * Finds review by ID, updates text and stars count,
   * saves changes to database.
   *
   * @param reviewId ID of review to update
   * @param updateReviewDTO new review data (text, stars)
   */
  void updateReview(Long reviewId, CreateReviewDTO updateReviewDTO);

  List<ReviewDTO> getUserReview(Long userId);

  void deleteReviewById(Long id);
}
