// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.doris.flink;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.flink.table.api.Expressions.$;

public class DorisSinkExample {

    public static void main(String[] args) {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.disableOperatorChaining();
        final StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        List<Tuple2<String, Integer>> data = new ArrayList<>();
        data.add(new Tuple2<>("doris",1));
//        DataStreamSource<Tuple2<String, Integer>> source = env.fromCollection(data);
        DataStreamSource<Tuple2<String, Integer>> source = env.addSource(new SourceFunction<Tuple2<String, Integer>>() {
            private Integer id = 0;
            @Override
            public void run(SourceContext<Tuple2<String, Integer>> out) throws Exception {
                while(true){
                    id=id+1;
                   // String record = UUID.randomUUID() + "," + id + ",test";
                    out.collect(new Tuple2<>(UUID.randomUUID().toString(), id));
                    Thread.sleep(100);
                }
            }
            @Override
            public void cancel() {

            }});


        tEnv.createTemporaryView("doris_test",source,$("name"),$("age"));

        tEnv.executeSql(
                "CREATE TABLE doris_test_sink (" +
                        "name STRING," +
                        "age INT" +
                        ") " +
                        "WITH (\n" +
                        "  'connector' = 'doris',\n" +
                        "  'fenodes' = '127.0.0.1:8030',\n" +
                        "  'table.identifier' = 'test.test_flink',\n" +
                        "  'username' = 'root',\n" +
                        "  'password' = '',\n" +
                        "  'sink.batch.size' = '2',\n" +
                        "  'sink.batch.interval' = '10000',\n" +
                        "  'sink.properties.format' = 'json',\n" +
                        "  'sink.properties.strip_outer_array' = 'true'\n" +
                        ")");

        tEnv.executeSql("INSERT INTO doris_test_sink select name,age from doris_test");
    }
}
