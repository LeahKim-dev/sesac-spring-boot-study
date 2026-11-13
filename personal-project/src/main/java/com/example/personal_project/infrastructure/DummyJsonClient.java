package com.example.personal_project.infrastructure;

import java.net.http.HttpClient;
import java.time.Duration;

import com.sun.net.httpserver.HttpsConfigurator;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DummyJsonClient {
    // RestClient: 자바 코드에서 HTTP 요청을 보내는 도구
    private final RestClient restClient;

    DummyJsonClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public DummyJsonClient() {
        // HttpClient: 연결 제한 3초 설정
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        // factory: 그 HttpClient를 사용하고, 응답 제한 5초 설정
        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        // RestClient: factory의 설정으로 요청
        this.restClient = RestClient.builder() // 요청 도구를 만들기 위한 설정을 시작
                .baseUrl("https://dummyjson.com") // 요청할 사이트의 기본 주소
                .requestFactory(factory) // 앞에서 설정한 연결 3초·응답 5초 제한을 이 요청 도구에서 사용해
                .build(); // 설정한 내용으로 실제 RestClient 객체를 만들기
    }

    public DummyJsonProduct findById(long id) {
        // 1. id 검사
        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, // 400
                    "상품 id는 양수여야 합니다."); // DummyJSON에 요청하기 전에 거부
        }

        try {
            // 2. 외부 요청
            DummyJsonProduct product = restClient.get() // DummyJSON 웹사이트에 GET 방식으로 요청할 준비
                    .uri("/products/{id}", id) // {id} 자리에 전달받은 id 넣기
                    .retrieve() // 응답을 처리하는 단계로
                    .body(DummyJsonProduct.class); // 실제 요청을 수행하고, 응답 JSON을 DummyJsonProduct 객체로 변환해서 받음

            // 3. 받은 상품이 null인지 검사
            if (product == null) {
                // 성공 응답이지만 본문이 비어 있음
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, // 502
                        "외부 상품 응답이 비어 있습니다."
                );
            }
            //정상 상품 응답
            return product;

        } catch (RestClientResponseException exception) {
            // 외부 서버가 오류 상태로 응답한 경우
            // 외부 서버가 404 응답
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, //404
                        "외부 상품을 찾을 수 없습니다."
                );
            }
            // 외부 서버가 그 밖의 오류 응답
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 서버가 오류를 반환했습니다."
            );
        } catch (RestClientException exception) {
            // 연결 실패, 시간 초과 등 나머지 요청 오류
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 정보를 가져오지 못했습니다."
            );

        }
    }

    public DummyJsonProductList findAll(int limit, int skip) {
        // 입력 검사
        if (limit <1 || limit > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, // 400
                    "limit은 1 이상 20 이하여야 합니다."
            );
        }

        if (skip < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "skip은 0 이상이어야 합니다."
            );
        }
        try {
            // 외부 요청
            DummyJsonProductList response = restClient.get()// 마지막에 받아올 목록 응답 객체를 response라는 변수에 담겠다
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve() // 응답을 받아 처리
                    .body(DummyJsonProductList.class);

            // 응답 반환
            if (response == null) { // 요청이 끝났는데 받은 값이 null
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, //502
                        "외부 상품 목록 응답이 비어 있습니다."
                );
            }
            return response;
        } catch (RestClientException exception) { // 요청 도중 연결 실패 등의 예외 발생
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 목록을 가져오지 못했습니다.");
        }
    }
}
