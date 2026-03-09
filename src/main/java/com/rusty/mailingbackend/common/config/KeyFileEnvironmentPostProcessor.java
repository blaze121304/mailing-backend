package com.rusty.mailingbackend.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class KeyFileEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String KEY_FILE = "keyfile.md";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path keyFilePath = Paths.get(KEY_FILE);
        if (!Files.exists(keyFilePath)) {
            System.out.println("[KeyFile] keyfile.md 없음 - 환경변수로 대체됩니다.");
            return;
        }

        Map<String, Object> props = new HashMap<>();
        try {
            for (String line : Files.readAllLines(keyFilePath)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx < 0) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                props.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("[KeyFile] keyfile.md 읽기 실패: " + e.getMessage());
            return;
        }

        environment.getPropertySources().addFirst(new MapPropertySource("keyFileProperties", props));
        System.out.println("[KeyFile] keyfile.md 로드 완료 (" + props.size() + "개 키)");
    }
}