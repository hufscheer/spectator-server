package com.sports.server.common.application;

public interface AlertService {
    void sendErrorAlert(String path, String method, String errorMessage, Exception exception);
}
