package com.lxkplus.mybatisMaker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ahocorasick.trie.Token;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelectBody {
    List<ColumnWithJavaStatus> columnWithJavaStatusList = new ArrayList<>();
    @NotNull
    String whereBy = "";
    List<Token> trims = new ArrayList<>();
    boolean success = false;
}
