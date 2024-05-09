package com.lxkplus.mybatisMaker.service;

import com.lxkplus.mybatisMaker.enums.Package;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class PathService {

    @Value("${mybatis-maker.code-root}")
    String rootPath;

    public Path getPath(Object... pathName) {

        for (int i = 0; i < pathName.length - 1; i++) {
            if (pathName[i] instanceof Package p) {
                pathName[i] = p.getPackageName().replace(".", "/");
            }
        }
        return Path.of(rootPath, StringUtils.join(pathName, "/"));
    }

    public void createFile(Path path, String str) throws IOException {
        if (Files.isRegularFile(path)) {
            Files.writeString(path, str, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("刷新: " + path);
        }
        else if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, str, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("新建: " + path);
        } else {
            Files.writeString(path, str, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("新建: " + path);
        }
    }
}
