package io.zmyzheng;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zmyzheng.model.Session;
import io.zmyzheng.model.UploadedBody;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.*;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


/**
 * @Author: Mingyang Zheng
 * @Date: 2020-01-29 20:17
 */
public class StreamPipeline implements Serializable {
    private ObjectMapper objectMapper = new ObjectMapper();



    public void startPipeLine() {
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SparkSession spark = SparkSession
                .builder()
                .appName("streamJob")
                .getOrCreate();


        Dataset<Row> inputDf = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "b-2.mingyang2.1v5xcu.c2.kafka.us-west-2.amazonaws.com:9094,b-1.mingyang2.1v5xcu.c2.kafka.us-west-2.amazonaws.com:9094,b-3.mingyang2.1v5xcu.c2.kafka.us-west-2.amazonaws.com:9094")
                .option("subscribe", "events")
                .option("kafka.security.protocol", "SSL")
                .load();



        Dataset<Row> kafkaMeta = inputDf.selectExpr("partition", "offset");

        Dataset<UploadedBody> uploadedBodies = inputDf.selectExpr("CAST(value AS STRING)", "timestamp")
                .map( (MapFunction<Row, UploadedBody>) row -> {
                    try {
                        UploadedBody uploadedBody = objectMapper.<UploadedBody>readValue(row.<String>getAs("value"), UploadedBody.class);
                        uploadedBody.setProcessingTime(row.<Timestamp>getAs("timestamp"));
                        return uploadedBody;
                    } catch (Exception e) {
                        return null;
                    }

                }, Encoders.bean(UploadedBody.class))
                .filter((FilterFunction<UploadedBody>) uploadBody -> uploadBody != null);


        Dataset<Session> sessions = uploadedBodies.flatMap((FlatMapFunction<UploadedBody, Session>) uploadedBody -> {
            uploadedBody.getPayload().forEach( session -> {
                session.setClientId(uploadedBody.getClientId());
                session.setApplianceId(uploadedBody.getApplianceId());
                session.setType(uploadedBody.getType());
                LocalDateTime localDateTime = LocalDateTime.ofInstant(uploadedBody.getProcessingTime().toInstant(), ZoneId.of("Etc/UTC"));
                session.setIndexName("processed-events-" + localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));
            });
            return uploadedBody.getPayload().iterator();
        }, Encoders.bean(Session.class) );


        StreamingQuery streamingQuery0 = kafkaMeta.writeStream()
                .outputMode("append")
                .format("csv")
                .option("path", "s3a://fs-ds-dev/mingyang-data-pod12/sink0/")
                .option("checkpointLocation", "/tmp/checkpoint/test0")
                .start();


//        StreamingQuery streamingQuery = sessions.writeStream()
//                .outputMode("append")
//                .format("console")
//                .start();


//        StreamingQuery streamingQuery1 = sessions.writeStream()
//                .outputMode("append")
//                .format("org.elasticsearch.spark.sql")
//                .option("es.nodes", "https://search-mingyang-migration-3v2n7d3sxcwanvx6fql3pqizku.us-west-2.es.amazonaws.com:443")
//                .option("es.resource.write", "{indexName}/events")
//                .option("checkpointLocation", "/tmp/checkpoint/test")
//                .option("es.nodes.wan.only", "true")
//                .option("es.mapping.id", "id")
//                .start();
//
//        StreamingQuery streamingQuery2 = sessions.writeStream()
//                .partitionBy("indexName")
//                .outputMode("append")
//                .format("parquet")
//                .option("path", "s3a://fs-ds-dev/mingyang-data-pod12/sink/")
//                .option("checkpointLocation", "/tmp/checkpoint/test2")
//                .start();



        DataStreamWriter<Session> writer = sessions.writeStream()
                .outputMode("append")
                .foreachBatch((VoidFunction2<Dataset<Session>, Long>) (Dataset<Session> batchDF, Long batchId) -> {

                    batchDF.persist();

                    batchDF.write()
                            .format("org.elasticsearch.spark.sql")
                            .option("es.nodes", "https://search-mingyang-migration-3v2n7d3sxcwanvx6fql3pqizku.us-west-2.es.amazonaws.com:443")
                            .option("es.resource", "{indexName}/events")  //?
                            .option("checkpointLocation", "/tmp/checkpoint/test")
                            .option("es.nodes.wan.only", "true")
                            .option("es.mapping.id", "id")
                            .save();
                    batchDF.write()
                            .partitionBy("indexName")
                            .format("parquet").mode(SaveMode.Append)
                            .option("path", "s3a://fs-ds-dev/mingyang-data-pod12/sink2/")
                            .option("checkpointLocation", "/tmp/checkpoint/test2")  // ?
                            .save();

                    batchDF.unpersist();
                });

        StreamingQuery streamingQuery3 = writer.start();

        try {
//            streamingQuery1.awaitTermination();
//            streamingQuery2.awaitTermination();
            streamingQuery3.awaitTermination();
            streamingQuery0.awaitTermination();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


}
