package com.btw.tax_engine.quick_data_access.impl;

import com.btw.tax_engine.quick_data_access.FTRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FTRepoImpl implements FTRepo, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(FTRepoImpl.class);

    private Environment env;
    private String remotePath;
    private String localPath;

    @Override
    public void download(String filename) {
        try {
            doDownload(filename, remotePath, localPath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void download(String[] filenames) {
        try {
            for (String filename : filenames) {
                doDownload(filename, remotePath, localPath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void doDownload(String filename, String remotePath, String localPath) throws IOException {
        String url = remotePath + filename;
        Path tmpPath = Paths.get(localPath, filename + ".partial");
        boolean flag = false;
        int max_retries = 5;
        long size = 0;

        while (max_retries >= 0) {
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(tmpPath.toFile());
                 FileChannel fc = fileOutputStream.getChannel()) {
                size = fc.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                if (size == 0) {
                    flag = false;
                    max_retries -= 1;
                    Thread.sleep(5000);
                    log.warn("Download {} failed, do {} retry...", filename, 5 - max_retries);
                } else {
                    flag = true;
                    break;
                }
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
        log.info("Transfer from '{}' to '{} with {} bytes', {}",
                url, tmpPath.toAbsolutePath(), size, flag ? "success" : "failed");
        Path targetPath = Paths.get(localPath, filename);
        log.info("Move '{}' to '{}'", tmpPath.toAbsolutePath(),
                Files.move(tmpPath, targetPath, StandardCopyOption.REPLACE_EXISTING).toAbsolutePath());

    }

    @Autowired
    public void setEnv(Environment env) {
        this.env = env;
    }

    @Override
    public void afterPropertiesSet() {
        remotePath = env.getProperty("core.data.path");
        localPath = env.getProperty("java.io.tmpdir");
    }
}
