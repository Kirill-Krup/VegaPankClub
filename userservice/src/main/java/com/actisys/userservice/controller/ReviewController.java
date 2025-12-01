package com.actisys.userservice.controller;

import com.actisys.userservice.dto.ReviewResponseDtos.CreateReviewDTO;
import com.actisys.userservice.dto.ReviewResponseDtos.ReviewDTO;
import com.actisys.userservice.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @GetMapping("/getAllReviews")
  public ResponseEntity<List<ReviewDTO>> allReviews(){
    return ResponseEntity.ok(reviewService.getAllReviews());
  }

  @PostMapping("/addReview")
  public ResponseEntity<ReviewDTO> addReview(
      @RequestBody CreateReviewDTO createReviewDTO,
      @RequestHeader(value = "X-User-Id", required = false) String userId){
    return ResponseEntity.ok(reviewService.addReview(createReviewDTO, Long.parseLong(userId)));
  }

  @PutMapping({"/editVisibility/{reviewId}", "/deleteReview/{id}"})
  public ResponseEntity<Void> editVisibility(@PathVariable Long reviewId){
    reviewService.editVisibility(reviewId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/editReview/{reviewId}")
  public ResponseEntity<Void> editReview(@PathVariable Long reviewId,
      @RequestBody CreateReviewDTO updateReviewDTO){
    reviewService.updateReview(reviewId, updateReviewDTO);
    return ResponseEntity.noContent().build();
  }
}
