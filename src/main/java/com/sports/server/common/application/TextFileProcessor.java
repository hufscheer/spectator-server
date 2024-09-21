package com.sports.server.common.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class TextFileProcessor {

    public Set<String> readFile(String fileName, String delim) throws IOException {
        ClassPathResource resource = new ClassPathResource(fileName);

        Set<String> resultSet = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(delim);
                for (String token : tokens) {
                    resultSet.add(token.replaceAll("^[\\s'\"]+|[\\s'\"]+$", ""));
                }
            }
        }

        return resultSet;
    }

}

