package com.me.elasticjob;

import java.util.Arrays;
import java.util.List;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

public class MyDataflowJob<T> implements DataflowJob<T> {
    /**
     * 获取待处理数据.
     * 
     * @param shardingContext 分片上下文
     * @return 待处理的数据集合
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> fetchData(ShardingContext shardingContext) {
        return (List<T>) Arrays.asList("1", "2", "3");
    }

    /**
     * 处理数据.
     * 
     * @param shardingContext 分片上下文
     * @param data            待处理数据集合
     */
    @Override
    public void processData(ShardingContext shardingContext, List<T> data) {
        System.out.println("处理数据:" + data.toString());
    }

}
