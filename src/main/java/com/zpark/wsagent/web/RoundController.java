package com.zpark.wsagent.web;

import com.zpark.wsagent.service.AutonomousRunnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 协作回合运行控制器（前后端分离，供 React 前端调用）
 *
 * - 提供触发单轮或多轮运行的接口
 * - 入参与出参均使用JSON(键值对)格式，便于React前端通过fetch/axios调用
 *
 * 示例（React前端）：
 * fetch('/api/round/run', {
 *   method: 'POST',
 *   headers: { 'Content-Type': 'application/json' },
 *   body: JSON.stringify({ participantIds: [1,2,3], roundId: 1001 })
 * });
 */
@RestController
@RequestMapping("/api/round")
public class RoundController {

    private final AutonomousRunnerService runnerService;

    public RoundController(AutonomousRunnerService runnerService) {
        this.runnerService = runnerService;
    }

    /**
     * 触发单轮协作回合
     *
     * 请求JSON：
     * {
     *   "participantIds": [1,2,3],
     *   "roundId": 1001
     * }
     *
     * 响应JSON：
     * { "status": "ok", "roundId": 1001 }
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> run(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> idsNum = (List<Number>) body.get("participantIds");
        List<Long> participantIds = idsNum == null ? List.of() : idsNum.stream().map(Number::longValue).toList();
        Long roundId = body.get("roundId") == null ? null : ((Number) body.get("roundId")).longValue();

        runnerService.runRound(participantIds, roundId);
        return ResponseEntity.ok(Map.of("status", "ok", "roundId", roundId));
    }

    /**
     * 连续运行多轮（批量）
     *
     * 请求JSON：
     * {
     *   "participantIds": [1,2,3],
     *   "startRoundId": 2000,
     *   "rounds": 3      // 连续运行3轮：2000,2001,2002
     * }
     *
     * 响应JSON：
     * { "status": "ok", "roundsRun": 3 }
     */
    @PostMapping("/run-batch")
    public ResponseEntity<Map<String, Object>> runBatch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> idsNum = (List<Number>) body.get("participantIds");
        List<Long> participantIds = idsNum == null ? List.of() : idsNum.stream().map(Number::longValue).toList();
        Long startRoundId = body.get("startRoundId") == null ? null : ((Number) body.get("startRoundId")).longValue();
        Integer rounds = body.get("rounds") == null ? 1 : ((Number) body.get("rounds")).intValue();

        long base = startRoundId == null ? System.currentTimeMillis() : startRoundId;
        for (int i = 0; i < Math.max(1, rounds); i++) {
            long rid = base + i;
            runnerService.runRound(participantIds, rid);
        }
        return ResponseEntity.ok(Map.of("status", "ok", "roundsRun", Math.max(1, rounds)));
    }
}
