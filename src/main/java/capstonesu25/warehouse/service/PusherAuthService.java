package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Account;
import capstonesu25.warehouse.enums.AccountRole;
import capstonesu25.warehouse.repository.AccountRepository;
import capstonesu25.warehouse.utils.NotificationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PusherAuthService {
    private final AccountRepository accountRepository;
    private final com.pusher.rest.Pusher pusher;
    private static final Logger LOGGER = LoggerFactory.getLogger(PusherAuthService.class);

    /**
     * Authenticates a Pusher channel for the current user
     * @param channelName The Pusher channel name
     * @param socketId The socket ID from Pusher client
     * @return Map containing auth signature or error
     */
    public Map<String, Object> authenticatePusherChannel(String channelName, String socketId) {
        LOGGER.info("Authenticating user for channel: {}", channelName);
        
        Map<String, Object> response = new HashMap<>();
        
        if (channelName == null || socketId == null) {
            LOGGER.error("Missing channel_name or socket_id in request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing channel_name or socket_id");
        }
        
        if (!isAuthorized(channelName)) {
            LOGGER.error("User not authorized for channel: {}", channelName);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized for this channel");
        }
        
        // Use Pusher SDK to generate auth signature
        String auth = pusher.authenticate(socketId, channelName);
        response.put("auth", auth);
        
        return response;
    }

    /**
     * Checks if the authenticated user is allowed to subscribe to the given channel.
     * @param channelName The Pusher channel name.
     * @return true if authorized, false otherwise.
     */
    private boolean isAuthorized(String channelName) {
        Account account = getAuthenticatedAccount();
        if (account == null) {
            return false;
        }
        
        AccountRole role = account.getRole();
        // Role-based channel authorization
        switch (role) {
            case WAREHOUSE_MANAGER:
                return channelName.equals(NotificationUtil.WAREHOUSE_MANAGER_CHANNEL);
            case DEPARTMENT:
                return channelName.equals(NotificationUtil.DEPARTMENT_CHANNEL);
            case ACCOUNTING:
                return channelName.equals(NotificationUtil.ACCOUNTING_CHANNEL);
            case ADMIN:
                return channelName.equals(NotificationUtil.ADMIN_CHANNEL);
            case STAFF:
                return channelName.equals(NotificationUtil.STAFF_CHANNEL + account.getId());
            default:
                return false;
        }
    }

    /**
     * Returns the authenticated account, or null if not authenticated.
     */
    private Account getAuthenticatedAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return accountRepository.findByUsername(username).orElse(null);
    }
}
