package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.service.PusherAuthService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pusher")
@RequiredArgsConstructor
public class PusherAuthController {
    private final PusherAuthService pusherAuthService;
    private static final Logger LOGGER = LoggerFactory.getLogger(PusherAuthController.class);

    /**
     * Endpoint for Pusher client authentication (for private/presence channels).
     * Expects the channel name and socket_id from the Pusher client.
     */
    @Operation(summary = "Authenticate Pusher channel")
    @PostMapping("/auth")
    public ResponseEntity<?> authenticatePusherChannel(@RequestBody Map<String, String> body) {
        LOGGER.info("Authenticating Pusher channel: {}", body.get("channel_name"));
        
        String channelName = body.get("channel_name");
        String socketId = body.get("socket_id");
        
        Map<String, Object> response = pusherAuthService.authenticatePusherChannel(channelName, socketId);
        
        return ResponseUtil.getObject(
                response,
                HttpStatus.OK,
                "Channel authentication successful"
        );
    }
}
