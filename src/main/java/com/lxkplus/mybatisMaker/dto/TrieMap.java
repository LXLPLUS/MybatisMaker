package com.lxkplus.mybatisMaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ahocorasick.trie.Trie;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class TrieMap {
    Trie trie;
    /**
     * 列名和列的映射关系
     */
    HashMap<String, ColumnWithJavaStatus> ColumnMap;
}
