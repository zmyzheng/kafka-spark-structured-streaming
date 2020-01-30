package io.zmyzheng.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @Author: Mingyang Zheng
 * @Date: 2019-08-30 16:31
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session implements Serializable {

    @JsonProperty("_id")
    private String id;

    private String type;

    private String clientId;

    private String applianceId;
//    private Timestamp processingTime;

    private String indexName;

    private List<Long> srcGroups;

    private List<Long> dstGroups;

    private String srcIP;

    private String dstIP;

    private int port;

    private int protocol;

    private int count;

    private long startTime;

    private long endTime;

    private int policyId;

    private int policyVer;


}
