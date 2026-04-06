package com.myfitpet;

import com.myfitpet.pose.PoseApiProperties;
import com.myfitpet.chat.ChatApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({PoseApiProperties.class, ChatApiProperties.class})
public class MyfitpetServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyfitpetServerApplication.class, args);
    }
}
