package com.bemojr.mock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/response")
public class StatusCheckController {
    @GetMapping("200")
    public ResponseEntity<?> getOk() {
        return ResponseEntity.ok().body("Successful Request");
    }

    @GetMapping("500")
    public ResponseEntity<?> getServerError() {

        return ResponseEntity.internalServerError().build();
    }
}
