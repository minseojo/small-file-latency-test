package com.minseojo.demo;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

@RestController
@RequestMapping
@Slf4j
public class FileController {

    // 파일 전체를 JVM Heap 메모리에 올리는 방식 ㅡ byte[] 외, 모든 Object 동일 (String, 커스텀 객체 등)
    @GetMapping("/files/bytes/{id}")
    public ResponseEntity<byte[]> memory(@PathVariable String id) {

        return null;
    }

    // Resource: Spring이 대신 파일을 스트리밍 해줌
    /** Resource 구현체 종류
     FileSystemResource  : 로컬 파일
     ClassPathResource   : 클래스패스에 있는 리소스
     UrlResource         : URL/HTTP/FTP 같은 외부 소스
     ByteArrayResource   : 메모리에 있는 byte[] 로 Resource 흉내냄
     InputStreamResource : 임의의 InputStream 래핑
     */
    @GetMapping("/files/resource/{id}")
    public ResponseEntity<Resource> resource(@PathVariable String id) {

        return null;
    }

    // StreamingResponseBody: 콜백 기반 수동 스트리밍
    @GetMapping("/files/streaming/{id}")
    public ResponseEntity<StreamingResponseBody> streaming(@PathVariable String id) {

        return null;
    }

    // ResponseBodyEmitter: 이벤트 스트림/실시간 전송
    @GetMapping("/files/emitter/{id}")
    public ResponseEntity<ResponseBodyEmitter> emitter(@PathVariable String id) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send("chunk-1");
                emitter.send("chunk-2");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(emitter);
    }

    @GetMapping("/files/servlet/{id}")
    public void servlet(HttpServletResponse response, @PathVariable String id) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try (InputStream in = Files.newInputStream(Path.of("/data/" + id + ".bin"));
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
            out.flush();
        }
    }
}
