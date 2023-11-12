package com.sports.server.support.isolation;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class DatabaseIsolationExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        DatabaseManager databaseManager = getDatabaseManager(context);
        databaseManager.truncateTables();
    }

    private DatabaseManager getDatabaseManager(ExtensionContext context) {
        return (DatabaseManager) SpringExtension
                .getApplicationContext(context)
                .getBean("databaseManager");
    }
}
