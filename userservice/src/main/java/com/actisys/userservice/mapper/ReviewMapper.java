package com.actisys.userservice.mapper;

import com.actisys.userservice.dto.ReviewResponseDtos.ReviewDTO;
import com.actisys.userservice.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReviewMapper {
  @Mapping(target = "id", source = "id")
  @Mapping(target = "visible", source = "visible")
  ReviewDTO toDTO(Review review);
}
