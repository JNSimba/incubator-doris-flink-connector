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

import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisLookupOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.factories.DynamicTableSinkFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.utils.TableSchemaUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_BATCH_SIZE;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_DESERIALIZE_ARROW_ASYNC;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_DESERIALIZE_QUEUE_SIZE;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_EXEC_MEM_LIMIT;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_FILTER_QUERY;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_READ_FIELD;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_REQUEST_CONNECT_TIMEOUT_MS;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_REQUEST_QUERY_TIMEOUT_S;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_REQUEST_READ_TIMEOUT_MS;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_REQUEST_RETRIES;
import static org.apache.doris.flink.table.DorisConfigOptions.DORIS_TABLET_SIZE;
import static org.apache.doris.flink.table.DorisConfigOptions.LOAD_URL;
import static org.apache.doris.flink.table.DorisConfigOptions.IDENTIFIER;
import static org.apache.doris.flink.table.DorisConfigOptions.LOOKUP_CACHE_MAX_ROWS;
import static org.apache.doris.flink.table.DorisConfigOptions.LOOKUP_CACHE_TTL;
import static org.apache.doris.flink.table.DorisConfigOptions.LOOKUP_MAX_RETRIES;
import static org.apache.doris.flink.table.DorisConfigOptions.PASSWORD;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_BUFFER_COUNT;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_BUFFER_SIZE;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_CHECK_INTERVAL;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_ENABLE_2PC;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_ENABLE_DELETE;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_LABEL_PREFIX;
import static org.apache.doris.flink.table.DorisConfigOptions.SINK_MAX_RETRIES;
import static org.apache.doris.flink.table.DorisConfigOptions.SOURCE_USE_OLD_API;
import static org.apache.doris.flink.table.DorisConfigOptions.STREAM_LOAD_PROP_PREFIX;
import static org.apache.doris.flink.table.DorisConfigOptions.TABLE_IDENTIFIER;
import static org.apache.doris.flink.table.DorisConfigOptions.USERNAME;


/**
 * The {@link DorisDynamicTableFactory} translates the catalog table to a table source.
 *
 * <p>Because the table source requires a decoding format, we are discovering the format using the
 * provided {@link FactoryUtil} for convenience.
 */
public final class DorisDynamicTableFactory implements DynamicTableSourceFactory, DynamicTableSinkFactory {

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER; // used for matching to `connector = '...'`
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(LOAD_URL);
        options.add(TABLE_IDENTIFIER);
        return options;
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        final Set<ConfigOption<?>> options = new HashSet<>();
        options.add(LOAD_URL);
        options.add(TABLE_IDENTIFIER);
        options.add(USERNAME);
        options.add(PASSWORD);

        options.add(DORIS_READ_FIELD);
        options.add(DORIS_FILTER_QUERY);
        options.add(DORIS_TABLET_SIZE);
        options.add(DORIS_REQUEST_CONNECT_TIMEOUT_MS);
        options.add(DORIS_REQUEST_READ_TIMEOUT_MS);
        options.add(DORIS_REQUEST_QUERY_TIMEOUT_S);
        options.add(DORIS_REQUEST_RETRIES);
        options.add(DORIS_DESERIALIZE_ARROW_ASYNC);
        options.add(DORIS_DESERIALIZE_QUEUE_SIZE);
        options.add(DORIS_BATCH_SIZE);
        options.add(DORIS_EXEC_MEM_LIMIT);
        options.add(LOOKUP_CACHE_MAX_ROWS);
        options.add(LOOKUP_CACHE_TTL);
        options.add(LOOKUP_MAX_RETRIES);

        options.add(SINK_CHECK_INTERVAL);
        options.add(SINK_ENABLE_2PC);
        options.add(SINK_MAX_RETRIES);
        options.add(SINK_ENABLE_DELETE);
        options.add(SINK_LABEL_PREFIX);
        options.add(SINK_BUFFER_SIZE);
        options.add(SINK_BUFFER_COUNT);

        options.add(SOURCE_USE_OLD_API);
        return options;
    }

    @Override
    public DynamicTableSource createDynamicTableSource(Context context) {
        // either implement your custom validation logic here ...
        // or use the provided helper utility
        final FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
        // validate all options
        helper.validateExcept(STREAM_LOAD_PROP_PREFIX);
        // get the validated options
        final ReadableConfig options = helper.getOptions();
        // derive the produced data type (excluding computed columns) from the catalog table
        final DataType producedDataType = context.getCatalogTable().getSchema().toPhysicalRowDataType();
        TableSchema physicalSchema = TableSchemaUtils.getPhysicalSchema(context.getCatalogTable().getSchema());
        // create and return dynamic table source
        return new DorisDynamicTableSource(
                getDorisOptions(helper.getOptions()),
                getDorisReadOptions(helper.getOptions()),
                getDorisLookupOptions(helper.getOptions()),
                physicalSchema);
    }

    private DorisOptions getDorisOptions(ReadableConfig readableConfig) {
        final String fenodes = readableConfig.get(LOAD_URL);
        final DorisOptions.Builder builder = DorisOptions.builder()
                .setLoadUrl(fenodes)
                .setTableIdentifier(readableConfig.get(TABLE_IDENTIFIER));

        readableConfig.getOptional(USERNAME).ifPresent(builder::setUsername);
        readableConfig.getOptional(PASSWORD).ifPresent(builder::setPassword);
        return builder.build();
    }

    private DorisReadOptions getDorisReadOptions(ReadableConfig readableConfig) {
        final DorisReadOptions.Builder builder = DorisReadOptions.builder();
        builder.setDeserializeArrowAsync(readableConfig.get(DORIS_DESERIALIZE_ARROW_ASYNC))
                .setDeserializeQueueSize(readableConfig.get(DORIS_DESERIALIZE_QUEUE_SIZE))
                .setExecMemLimit(readableConfig.get(DORIS_EXEC_MEM_LIMIT))
                .setFilterQuery(readableConfig.get(DORIS_FILTER_QUERY))
                .setReadFields(readableConfig.get(DORIS_READ_FIELD))
                .setRequestQueryTimeoutS(readableConfig.get(DORIS_REQUEST_QUERY_TIMEOUT_S))
                .setRequestBatchSize(readableConfig.get(DORIS_BATCH_SIZE))
                .setRequestConnectTimeoutMs(readableConfig.get(DORIS_REQUEST_CONNECT_TIMEOUT_MS))
                .setRequestReadTimeoutMs(readableConfig.get(DORIS_REQUEST_READ_TIMEOUT_MS))
                .setRequestRetries(readableConfig.get(DORIS_REQUEST_RETRIES))
                .setRequestTabletSize(readableConfig.get(DORIS_TABLET_SIZE))
                .setUseOldApi(readableConfig.get(SOURCE_USE_OLD_API));
        return builder.build();
    }

    private DorisExecutionOptions getDorisExecutionOptions(ReadableConfig readableConfig, Properties streamLoadProp) {
        final DorisExecutionOptions.Builder builder = DorisExecutionOptions.builder();
        builder.setCheckInterval(readableConfig.get(SINK_CHECK_INTERVAL));
        builder.setMaxRetries(readableConfig.get(SINK_MAX_RETRIES));
        builder.setBufferSize(readableConfig.get(SINK_BUFFER_SIZE));
        builder.setBufferCount(readableConfig.get(SINK_BUFFER_COUNT));
        builder.setLabelPrefix(readableConfig.get(SINK_LABEL_PREFIX));
        builder.setStreamLoadProp(streamLoadProp);
        builder.setDeletable(readableConfig.get(SINK_ENABLE_DELETE));
        if (!readableConfig.get(SINK_ENABLE_2PC)) {
            builder.disable2PC();
        }
        return builder.build();
    }

    private Properties getStreamLoadProp(Map<String, String> tableOptions) {
        final Properties streamLoadProp = new Properties();

        for (Map.Entry<String, String> entry : tableOptions.entrySet()) {
            if (entry.getKey().startsWith(STREAM_LOAD_PROP_PREFIX)) {
                String subKey = entry.getKey().substring(STREAM_LOAD_PROP_PREFIX.length());
                streamLoadProp.put(subKey, entry.getValue());
            }
        }
        return streamLoadProp;
    }

    private DorisLookupOptions getDorisLookupOptions(ReadableConfig readableConfig){
        final DorisLookupOptions.Builder builder = DorisLookupOptions.builder();
        builder.setCacheExpireMs(readableConfig.get(LOOKUP_CACHE_TTL).toMillis());
        builder.setCacheMaxSize(readableConfig.get(LOOKUP_CACHE_MAX_ROWS));
        builder.setMaxRetryTimes(readableConfig.get(LOOKUP_MAX_RETRIES));
        return builder.build();
    }

    @Override
    public DynamicTableSink createDynamicTableSink(Context context) {
        final FactoryUtil.TableFactoryHelper helper =
                FactoryUtil.createTableFactoryHelper(
                        this, context);

        // validate all options
        helper.validateExcept(STREAM_LOAD_PROP_PREFIX);

        Properties streamLoadProp = getStreamLoadProp(context.getCatalogTable().getOptions());
        TableSchema physicalSchema =
                TableSchemaUtils.getPhysicalSchema(context.getCatalogTable().getSchema());
        // create and return dynamic table source
        return new DorisDynamicTableSink(
                getDorisOptions(helper.getOptions()),
                getDorisReadOptions(helper.getOptions()),
                getDorisExecutionOptions(helper.getOptions(), streamLoadProp),
                physicalSchema
        );
    }
}
