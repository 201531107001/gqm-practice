package com.me.elasticjob;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

public class MySimpleJob implements SimpleJob {

    /**
     * 执行作业.
     * 
     * @param shardingContext 分片上下文
     */
    @Override
    public void execute(ShardingContext context) {
        System.out.println(String.format("------Thread ID: %s, 任务总片数: %s, 当前分片项: %s",  
                Thread.currentThread().getId(), context.getShardingTotalCount(), 
                context.getShardingItem()));
        System.out.println("==========================================");
    }

}
