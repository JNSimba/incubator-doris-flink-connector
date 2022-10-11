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

package org.apache.doris.flink.table;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;

import java.time.Duration;

import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_BATCH_SIZE_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_DESERIALIZE_ARROW_ASYNC_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_DESERIALIZE_QUEUE_SIZE_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_EXEC_MEM_LIMIT_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_REQUEST_CONNECT_TIMEOUT_MS_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_REQUEST_QUERY_TIMEOUT_S_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_REQUEST_READ_TIMEOUT_MS_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_REQUEST_RETRIES_DEFAULT;
import static org.apache.doris.flink.cfg.ConfigurationOptions.DORIS_TABLET_SIZE_DEFAULT;

public class DorisConfigOptions {

    public static final String IDENTIFIER = "selectdb";
    // common option
    public static final ConfigOption<String> LOAD_URL = ConfigOptions.key("load-url").stringType().noDefaultValue().withDescription("doris load http address.");
    public static final ConfigOption<String> TABLE_IDENTIFIER = ConfigOptions.key("table.identifier").stringType().noDefaultValue().withDescription("the doris table name.");
    public static final ConfigOption<String> USERNAME = ConfigOptions.key("username").stringType().noDefaultValue().withDescription("the doris user name.");
    public static final ConfigOption<String> PASSWORD = ConfigOptions.key("password").stringType().noDefaultValue().withDescription("the doris password.");

    // source config options
    public static final ConfigOption<String> DORIS_READ_FIELD = ConfigOptions
            .key("doris.read.field")
            .stringType()
            .noDefaultValue()
            .withDescription("List of column names in the Doris table, separated by commas");
    public static final ConfigOption<String> DORIS_FILTER_QUERY = ConfigOptions
            .key("doris.filter.query")
            .stringType()
            .noDefaultValue()
            .withDescription("Filter expression of the query, which is transparently transmitted to Doris. Doris uses this expression to complete source-side data filtering");
    public static final ConfigOption<Integer> DORIS_TABLET_SIZE = ConfigOptions
            .key("doris.request.tablet.size")
            .intType()
            .defaultValue(DORIS_TABLET_SIZE_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Integer> DORIS_REQUEST_CONNECT_TIMEOUT_MS = ConfigOptions
            .key("doris.request.connect.timeout.ms")
            .intType()
            .defaultValue(DORIS_REQUEST_CONNECT_TIMEOUT_MS_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Integer> DORIS_REQUEST_READ_TIMEOUT_MS = ConfigOptions
            .key("doris.request.read.timeout.ms")
            .intType()
            .defaultValue(DORIS_REQUEST_READ_TIMEOUT_MS_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Integer> DORIS_REQUEST_QUERY_TIMEOUT_S = ConfigOptions
            .key("doris.request.query.timeout.s")
            .intType()
            .defaultValue(DORIS_REQUEST_QUERY_TIMEOUT_S_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Integer> DORIS_REQUEST_RETRIES = ConfigOptions
            .key("doris.request.retries")
            .intType()
            .defaultValue(DORIS_REQUEST_RETRIES_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Boolean> DORIS_DESERIALIZE_ARROW_ASYNC = ConfigOptions
            .key("doris.deserialize.arrow.async")
            .booleanType()
            .defaultValue(DORIS_DESERIALIZE_ARROW_ASYNC_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Integer> DORIS_DESERIALIZE_QUEUE_SIZE = ConfigOptions
            .key("doris.request.retriesdoris.deserialize.queue.size")
            .intType()
            .defaultValue(DORIS_DESERIALIZE_QUEUE_SIZE_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Integer> DORIS_BATCH_SIZE = ConfigOptions
            .key("doris.batch.size")
            .intType()
            .defaultValue(DORIS_BATCH_SIZE_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Long> DORIS_EXEC_MEM_LIMIT = ConfigOptions
            .key("doris.exec.mem.limit")
            .longType()
            .defaultValue(DORIS_EXEC_MEM_LIMIT_DEFAULT)
            .withDescription("");
    public static final ConfigOption<Boolean> SOURCE_USE_OLD_API = ConfigOptions
            .key("source.use-old-api")
            .booleanType()
            .defaultValue(false)
            .withDescription("Whether to read data using the new interface defined according to the FLIP-27 specification,default false");

    // Lookup options
    public static final ConfigOption<Long> LOOKUP_CACHE_MAX_ROWS =
            ConfigOptions.key("lookup.cache.max-rows")
                    .longType()
                    .defaultValue(-1L)
                    .withDescription(
                            "The max number of rows of lookup cache, over this value, the oldest rows will "
                                    + "be eliminated. \"cache.max-rows\" and \"cache.ttl\" options must all be specified if any of them is "
                                    + "specified.");

    public static final ConfigOption<Duration> LOOKUP_CACHE_TTL =
            ConfigOptions.key("lookup.cache.ttl")
                    .durationType()
                    .defaultValue(Duration.ofSeconds(10))
                    .withDescription("The cache time to live.");

    public static final ConfigOption<Integer> LOOKUP_MAX_RETRIES =
            ConfigOptions.key("lookup.max-retries")
                    .intType()
                    .defaultValue(3)
                    .withDescription("The max retry times if lookup database failed.");

    // sink config options
    public static final ConfigOption<Boolean> SINK_ENABLE_2PC = ConfigOptions
            .key("sink.enable-2pc")
            .booleanType()
            .defaultValue(true)
            .withDescription("enable 2PC while loading");

    public static final ConfigOption<Integer> SINK_CHECK_INTERVAL = ConfigOptions
            .key("sink.check-interval")
            .intType()
            .defaultValue(10000)
            .withDescription("check exception with the interval while loading");
    public static final ConfigOption<Integer> SINK_MAX_RETRIES = ConfigOptions
            .key("sink.max-retries")
            .intType()
            .defaultValue(3)
            .withDescription("the max retry times if writing records to database failed.");
    public static final ConfigOption<Integer> SINK_BUFFER_SIZE = ConfigOptions
            .key("sink.buffer-size")
            .intType()
            .defaultValue(256 * 1024)
            .withDescription("the buffer size to cache data for stream load.");
    public static final ConfigOption<Integer> SINK_BUFFER_COUNT = ConfigOptions
            .key("sink.buffer-count")
            .intType()
            .defaultValue(3)
            .withDescription("the buffer count to cache data for stream load.");
    public static final ConfigOption<String> SINK_LABEL_PREFIX = ConfigOptions
            .key("sink.label-prefix")
            .stringType()
            .defaultValue("")
            .withDescription("the unique label prefix.");
    public static final ConfigOption<Boolean> SINK_ENABLE_DELETE = ConfigOptions
            .key("sink.enable-delete")
            .booleanType()
            .defaultValue(true)
            .withDescription("whether to enable the delete function");

    // Prefix for Doris StreamLoad specific properties.
    public static final String STREAM_LOAD_PROP_PREFIX = "sink.properties.";

}
