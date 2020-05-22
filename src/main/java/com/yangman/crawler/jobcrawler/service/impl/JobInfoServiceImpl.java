package com.yangman.crawler.jobcrawler.service.impl;

import com.yangman.crawler.jobcrawler.dao.JobInfoDao;
import com.yangman.crawler.jobcrawler.pojo.JobInfo;
import com.yangman.crawler.jobcrawler.service.JobInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: Felix Yang (yangman)
 * @create: 2020-05-21 10:59
 * @description:
 **/

@Service
public class JobInfoServiceImpl implements JobInfoService {

    @Autowired
    private JobInfoDao jobInfoDao;


    @Override
    @Transactional
    public void save(JobInfo jobInfo) {
        //根据url和发布时间查询数据
        JobInfo param = new JobInfo();
        param.setUrl(jobInfo.getUrl());
        param.setTime(jobInfo.getTime());

        List<JobInfo> result = this.findJobInfo(param);

        // 如果数据不存在或者数据有更新，更新数据库
        if (result.size() == 0) {
            this.jobInfoDao.saveAndFlush(jobInfo);
        }

    }

    @Override
    public List<JobInfo> findJobInfo(JobInfo jobInfo) {

        // 设置查询条件
        Example example = Example.of(jobInfo);

        // 执行查询
        List<JobInfo> list = this.jobInfoDao.findAll(example);
        return list;
    }
}
