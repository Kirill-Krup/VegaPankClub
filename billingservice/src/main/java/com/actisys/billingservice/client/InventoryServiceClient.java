package com.actisys.billingservice.client;

import com.actisys.common.clientDtos.PcResponseDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "inventory-service",
    url = "${inventory.service.url}"
)
public interface InventoryServiceClient {
  @GetMapping("/api/v1/pcs/pcInfoByIds")
  List<PcResponseDTO> getPcInfoByIds(@RequestParam("ids") List<Long> ids);
}
