package com.actisys.userservice.consumer;

import com.actisys.common.user.CreateWalletEvent;
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
    public void handleCreateWallet(CreateWalletEvent createWalletEvent) {
        log.info("WALLET_CREATE EVENT: {}", createWalletEvent);
        userService.withdrawMoney(createWalletEvent);


    }
}
