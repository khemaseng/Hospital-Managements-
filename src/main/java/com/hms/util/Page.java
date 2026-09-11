package com.hms.util;

import java.util.List;

/**
 * A single page of results plus enough metadata for the UI to render
 * page-number controls without a second round-trip to the database.
 */
public record Page<T>(List<T> items, int pageIndex, int pageSize, int totalItems) {

    public int totalPages() {
        return Math.max(1, (int) Math.ceil(totalItems / (double) pageSize));
    }

    public boolean hasPrevious() {
        return pageIndex > 0;
    }

    public boolean hasNext() {
        return pageIndex < totalPages() - 1;
    }
}
