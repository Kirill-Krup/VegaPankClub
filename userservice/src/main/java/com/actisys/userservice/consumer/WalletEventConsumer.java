package com.actisys.userservice.consumer;

import com.actisys.common.events.user.CreateWalletEvent;
import com.actisys.common.events.user.RefundMoneyEvent;
import com.actisys.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class WalletEventConsumer {
    private final UserService userService;

    @KafkaListener(topics = "CREATE_WALLET_EVENT", groupId = "user-service-group")
    public void handleCreateWalletEvent(CreateWalletEvent createWalletEvent) {
        log.info("WALLET_CREATE EVENT: {}", createWalletEvent);
        userService.withdrawMoney(createWalletEvent);
    }

    @KafkaListener(topics = "CREATE_WALLET_REPLENISHMENT_EVENT", groupId = "user-service-group")
    public void handleCreateWalletReplenishmentEvent(CreateWalletEvent createWalletEvent) {
         log.info("WALLET_CREATE_REPLENISHMENT_EVENT: {}", createWalletEvent);
         userService.replenishmentMoney(createWalletEvent);
    }

    @KafkaListener(topics = "REFUND_MONEYS_EVENT", groupId = "user-service-group")
    public void handleRefundMoneyEvent(RefundMoneyEvent refundMoneyEvent) {
       log.info("WALLET_REFUND_MONEYS_EVENT: {}", refundMoneyEvent);
       userService.refundMoneys(refundMoneyEvent);
    }

}
