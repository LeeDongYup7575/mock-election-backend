package com.example.mockvoting.domain.pollingStation.controller;

import com.example.mockvoting.domain.pollingStation.service.PollingStationProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 공공데이터 API를 통해 투표소 및 사전투표소 정보를 조회하는 컨트롤러입니다.
@RestController
@RequestMapping("/api/polling")
public class PollingStationProxyController {

    private final PollingStationProxyService pollingStationProxyService;

    @Autowired
    public PollingStationProxyController(PollingStationProxyService pollingStationProxyService) {
        this.pollingStationProxyService = pollingStationProxyService;
    }

    // 투표소 정보 응답
    @GetMapping("/getPolplcOtlnmapTrnsportInfoInqire")
    public ResponseEntity<Object> getPollingStations(
            @RequestParam Map<String, String> params) {
        return pollingStationProxyService.getPollingStationsData(params);
    }

    // 사전 투표소 정보 응답
    @GetMapping("/getPrePolplcOtlnmapTrnsportInfoInqire")
    public ResponseEntity<Object> getPrePollingStations(
            @RequestParam Map<String, String> params) {
        return pollingStationProxyService.getPrePollingStationsData(params);
    }

}