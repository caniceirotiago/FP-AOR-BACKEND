package aor.fpbackend.bean;

import aor.fpbackend.dao.MethodDao;
import aor.fpbackend.entity.MethodEntity;
import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.exception.DatabaseOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MethodBeanTest {

    @InjectMocks
    private MethodBean methodBean;

    @Mock
    private MethodDao methodDao;

    private List<LogEvent> logEvents;
    private Appender mockAppender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup a custom appender to capture log messages
        logEvents = new ArrayList<>();
        mockAppender = new AbstractAppender("MockAppender", null, PatternLayout.createDefaultLayout(), false) {
            @Override
            public void append(LogEvent event) {
                logEvents.add(event);
            }
        };

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.addAppender(mockAppender, null, null);
        mockAppender.start();
        ctx.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.removeAppender("MockAppender");
        mockAppender.stop();
        ctx.updateLoggers();
    }

    @Test
    void testCreateMethodIfNotExistent_MethodDoesNotExist() throws DatabaseOperationException {
        MethodEnum methodName = MethodEnum.UPDATE_ROLE;
        String description = "Test description";
        long id = 1L;

        when(methodDao.checkMethodExist(methodName)).thenReturn(false);

        methodBean.createMethodIfNotExistent(methodName, description, id);

        verify(methodDao, times(1)).persist(any(MethodEntity.class));
        assertTrue(logEvents.stream().anyMatch(event -> event.getMessage().getFormattedMessage().contains("Method " + methodName + " created successfully.")));
    }

    @Test
    void testCreateMethodIfNotExistent_MethodAlreadyExists() throws DatabaseOperationException {
        MethodEnum methodName = MethodEnum.UPDATE_ROLE;
        String description = "Test description";
        long id = 1L;

        when(methodDao.checkMethodExist(methodName)).thenReturn(true);

        methodBean.createMethodIfNotExistent(methodName, description, id);

        verify(methodDao, never()).persist(any(MethodEntity.class));
        assertTrue(logEvents.stream().anyMatch(event -> event.getMessage().getFormattedMessage().contains("Method " + methodName + " already exists, creation skipped.")));
    }

    @Test
    void testCreateMethodIfNotExistent_DatabaseOperationException() {
        MethodEnum methodName = MethodEnum.UPDATE_ROLE;
        String description = "Test description";
        long id = 1L;

        when(methodDao.checkMethodExist(methodName)).thenThrow(new RuntimeException("Database error"));

        assertThrows(DatabaseOperationException.class, () -> methodBean.createMethodIfNotExistent(methodName, description, id));

        assertTrue(logEvents.stream().anyMatch(event -> event.getMessage().getFormattedMessage().contains("Error creating method " + methodName + ": Database error")));
    }
}
