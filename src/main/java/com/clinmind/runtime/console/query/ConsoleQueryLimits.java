package com.clinmind.runtime.console.query;

import com.clinmind.runtime.api.ApiException;
import org.springframework.http.HttpStatus;

final class ConsoleQueryLimits {

    static final int DEFAULT_LIMIT = 50;
    static final int MAX_LIMIT = 200;

    private ConsoleQueryLimits() {
    }

    static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "CONSOLE_QUERY_INVALID",
                    "limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }
}
