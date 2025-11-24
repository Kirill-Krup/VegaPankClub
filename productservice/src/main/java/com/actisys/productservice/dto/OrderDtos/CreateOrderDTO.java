package com.actisys.productservice.dto.OrderDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDTO {
    @NotNull(message = "Total cost is required")
    @DecimalMin(value = "0.01", message = "Total cost must be greater than 0")
    private BigDecimal totalCost;

    private List<OrderItemDTO> orderItems;
}
