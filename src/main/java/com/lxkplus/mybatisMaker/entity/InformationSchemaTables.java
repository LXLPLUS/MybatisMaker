package com.lxkplus.mybatisMaker.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * information_schema.TABLES
 */
@Data
@NoArgsConstructor
public class InformationSchemaTables implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private String tableCatalog;
  private String tableSchema;
  private String tableName;
  private String tableType;
  private String engine;
  private Long version;
  private String rowFormat;
  private Long tableRows;
  private Long avgRowLength;
  private Long dataLength;
  private Long maxDataLength;
  private Long indexLength;
  private Long dataFree;
  private Long autoIncrement;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private LocalDateTime checkTime;
  private String tableCollation;
  private Long checksum;
  private String createOptions;
  private String tableComment;
}
