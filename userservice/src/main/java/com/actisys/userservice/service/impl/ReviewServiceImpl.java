package com.actisys.userservice.service.impl;

import com.actisys.userservice.dto.ReviewResponseDtos.CreateReviewDTO;
import com.actisys.userservice.dto.ReviewResponseDtos.ReviewDTO;
import com.actisys.userservice.exception.ReviewNotFoundException;
import com.actisys.userservice.exception.UserNotFoundException;
import com.actisys.userservice.mapper.ReviewMapper;
import com.actisys.userservice.model.Review;
import com.actisys.userservice.model.User;
import com.actisys.userservice.repository.ReviewRepository;
import com.actisys.userservice.repository.UserRepository;
import com.actisys.userservice.service.ReviewService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final UserRepository userRepository;

  @Override
  public List<ReviewDTO> getAllReviews() {
    List<Review> allReviews = reviewRepository.findAll();
    return allReviews.stream()
        .map(reviewMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public ReviewDTO addReview(CreateReviewDTO createReviewDTO, Long userId) {
    User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
    Review review = new Review();
    review.setUser(user);
    review.setStars(createReviewDTO.getStars());
    review.setReviewText(createReviewDTO.getReviewText());
    return reviewMapper.toDTO(reviewRepository.save(review));
  }

  @Override
  public void editVisibility(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(()-> new ReviewNotFoundException(reviewId));
    review.setVisible(!review.isVisible());
    reviewRepository.save(review);
  }

  @Override
  public void updateReview(Long reviewId, CreateReviewDTO updateReviewDTO) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(()-> new ReviewNotFoundException(reviewId));
    review.setReviewText(updateReviewDTO.getReviewText());
    review.setStars(updateReviewDTO.getStars());
    reviewRepository.save(review);
  }

  @Override
  public List<ReviewDTO> getUserReview(Long userId) {
    return reviewRepository.findAllByUserId(userId)
        .stream()
        .map(reviewMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteReviewById(Long id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(()-> new ReviewNotFoundException(id));
    reviewRepository.delete(review);
  }
}
