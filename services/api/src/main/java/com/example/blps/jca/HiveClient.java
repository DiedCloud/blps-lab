package com.example.blps.jca;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.example.blps.dao.controller.model.HiveApiResponseDTO;
import com.example.blps.dao.controller.model.HiveRequestDTO;
import com.example.blps.exception.HiveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class HiveClient {
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public HiveClient(
            RestTemplateBuilder restTemplateBuilder,
            String baseUrl,
            String apiPath,
            String apiKey
    ) {
        this.restTemplate = restTemplateBuilder
                .build();
        this.apiUrl = baseUrl + apiPath;
        this.apiKey = apiKey;
    }

    public Optional<HiveApiResponseDTO> moderate(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

            HttpEntity<HiveRequestDTO> entity = new HttpEntity<>(new HiveRequestDTO(List.of(new HiveRequestDTO.HiveInput(text))), headers);
            ResponseEntity<HiveApiResponseDTO> response = restTemplate.postForEntity(
                    apiUrl,
                    entity,
                    HiveApiResponseDTO.class
            );

            if (
                    response.getStatusCode() == HttpStatus.OK && response.getBody() != null
            ) {
                HiveApiResponseDTO bankInfo = response.getBody();
                return Optional.of(bankInfo);
            } else {
                return Optional.empty(); // Не ОК или пустое тело - считаем, что информации нет
            }
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new HiveException(
                    "Client error: " +
                            e.getStatusCode(), e
            );
        } catch (HttpServerErrorException e) { // 5xx ошибки
            throw new HiveException(
                    "Hive server error: " +
                            e.getStatusCode(),
                    e
            );
        } catch (ResourceAccessException e) { // Ошибки соединения, таймауты
            throw new HiveException(
                    "Hive connection failed",
                    e
            );
        } catch (RestClientException e) { // Общий случай для других ошибок RestTemplate
            throw new HiveException(
                    "Unexpected error communicating with Hive",
                    e
            );
        }
    }
}