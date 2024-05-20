package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.dto.TableFlowContext;

import java.io.IOException;

public interface FileCreateService {

    boolean generate();

    void createFile(TableFlowContext table) throws IOException;
}
