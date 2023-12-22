package org.apache.doris.flink.sink.metrics;

import org.apache.doris.flink.rest.models.RespContent;
import org.apache.doris.flink.sink.writer.serializer.DorisRecord;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.groups.SinkWriterMetricGroup;

import java.io.Serializable;

public class DorisSinkMetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String NUMBER_TOTAL_ROWS = "totalNumberTotalRows";
    public static final String NUMBER_LOADED_ROWS = "totalNumberLoadedRows";
    public static final String LOAD_BYTES = "totalLoadBytes";
    private String tableIdentifier;
    private Counter numberTotalRows;
    private Counter numberLoadedRows;
    private Counter loadBytes;

    public DorisSinkMetrics(String tableIdentifier) {
        this.tableIdentifier = tableIdentifier;
    }

    public static DorisSinkMetrics of(String tableIdentifier){
         return new DorisSinkMetrics(tableIdentifier);
    }

    public void register(SinkWriterMetricGroup sinkMetricGroup, int subtaskId){
        this.numberTotalRows = sinkMetricGroup.counter(String.format("%s_%s_%s", tableIdentifier, subtaskId, NUMBER_TOTAL_ROWS));
        this.numberLoadedRows = sinkMetricGroup.counter(String.format("%s_%s_%s", tableIdentifier, subtaskId, NUMBER_LOADED_ROWS));
        this.loadBytes = sinkMetricGroup.counter(String.format("%s_%s_%s", tableIdentifier, subtaskId, LOAD_BYTES));
    }

    public void refresh(RespContent respContent){
        numberTotalRows.inc(respContent.getNumberTotalRows());
        numberLoadedRows.inc(respContent.getNumberLoadedRows());
        loadBytes.inc(respContent.getLoadBytes());
    }
}
