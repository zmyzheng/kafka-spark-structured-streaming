package io.zmyzheng.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @Author: Mingyang Zheng
 * @Date: 2019-08-30 16:30
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedBody implements Serializable {
    private String type;

    private String clientId;

    private String applianceId;

    private Timestamp processingTime;

    private List<Session> payload;


}
