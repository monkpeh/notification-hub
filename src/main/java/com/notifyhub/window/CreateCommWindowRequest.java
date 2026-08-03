package com.notifyhub.window;

import java.time.LocalTime;

public record CreateCommWindowRequest(
    LocalTime startWindow,
    LocalTime endWindow,
    String occurrence
) {}
