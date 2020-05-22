package com.yangman.crawler.jobcrawler.task;

import com.yangman.crawler.jobcrawler.pojo.JobInfo;
import com.yangman.crawler.jobcrawler.service.JobInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @author: Felix Yang (yangman)
 * @create: 2020-05-22 14:44
 * @description:
 **/

@Component
public class SpringDataPipline implements Pipeline {

    @Autowired
    private JobInfoService jobInfoService;

    @Override
    public void process(ResultItems resultItems, Task task) {
        // 获取封装好的招详情对象
        JobInfo jobInfo = resultItems.get("jobInfo");

        if (jobInfo != null) {
            this.jobInfoService.save(jobInfo);
        }
    }
}
