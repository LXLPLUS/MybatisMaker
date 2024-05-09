package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.dto.TableMessage;

import java.io.IOException;

public interface FileCreateService {

    void createFile(TableMessage table) throws IOException;
}
