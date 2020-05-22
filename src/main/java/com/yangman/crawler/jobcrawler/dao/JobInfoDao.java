package com.yangman.crawler.jobcrawler.dao;

import com.yangman.crawler.jobcrawler.pojo.JobInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobInfoDao extends JpaRepository<JobInfo, Long> {
}
