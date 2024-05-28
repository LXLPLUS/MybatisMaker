package com.lxkplus.mybatisMaker.dto;

import com.lxkplus.mybatisMaker.enums.MapperType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ahocorasick.trie.Token;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelectBody {
    /**
     * 需要注入的参数对应列
     */
    List<ColumnWithJavaStatus> columnWithJavaStatusList = new ArrayList<>();
    @NotNull
    String whereBy = "";
    /**
     * 被切分的token
     */
    List<Token> trims = new ArrayList<>();
    /**
     * 是否生成成功，如果成功才会进行下一步
     */
    boolean success = false;
    /**
     * 需要mapper绑定的类型是单值还是集合
     */
    MapperType mapperType = MapperType.SINGLE;
}
