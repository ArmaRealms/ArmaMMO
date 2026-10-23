package com.gmail.nossr50.api.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;

public class IncompleteNamespacedKeyRegister extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6905157273569301219L;

    public IncompleteNamespacedKeyRegister(@NotNull String message) {
        super(message);
    }
}
