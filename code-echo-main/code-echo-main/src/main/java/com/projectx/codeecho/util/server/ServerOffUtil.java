package com.projectx.codeecho.util.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerOffUtil {
    // @Scheduled(cron = "0 0 */3 * * *")
    public void stopTomcatTest() throws Exception {
        log.info("[ServerOffUtil] 서버 종료");
        System.exit(1);
    }
}