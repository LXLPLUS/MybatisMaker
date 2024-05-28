package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;

public interface FileCreateService {

    boolean needGenerate();

    void createFile(@NotNull TableFlowContext table) throws IOException;
}
