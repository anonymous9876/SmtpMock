package com.example.smtpmock.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SmtpServerLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServerLifecycle.class);

    private final MockMessageListener messageListener;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int port;
    private SMTPServer smtpServer;

    public SmtpServerLifecycle(MockMessageListener messageListener,
                               @Value("${smtp.mock.port:2525}") int port) {
        this.messageListener = messageListener;
        this.port = port;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener));
            smtpServer.setPort(port);
            smtpServer.setSoftwareName("Spring SMTP Mock");
            smtpServer.start();
            LOGGER.info("Started SMTP mock server on port {}", port);
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false) && smtpServer != null) {
            smtpServer.stop();
            LOGGER.info("Stopped SMTP mock server");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }
}
