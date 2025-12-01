package com.actisys.userservice.mapper;

import com.actisys.userservice.dto.ReviewResponseDtos.ReviewDTO;
import com.actisys.userservice.model.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReviewMapper {
  ReviewDTO toDTO(Review review);
}
