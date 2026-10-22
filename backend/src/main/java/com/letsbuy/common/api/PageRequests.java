package com.letsbuy.common.api;

public final class PageRequests {

    private PageRequests() {
    }

    public static int clampPage(int page) {
        return Math.max(page, 0);
    }

    public static int clampSize(int size) {
        return Math.min(Math.max(size, 1), 48);
    }
}
