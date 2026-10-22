package com.letsbuy.common.api;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long total, boolean hasNext) {
}
