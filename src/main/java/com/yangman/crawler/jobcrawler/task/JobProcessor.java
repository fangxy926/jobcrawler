package com.yangman.crawler.jobcrawler.task;

import com.yangman.crawler.jobcrawler.pojo.JobInfo;
import com.yangman.crawler.jobcrawler.utils.MathSalary;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @author: Felix Yang (yangman)
 * @create: 2020-05-21 11:12
 * @description:
 **/

@Component
public class JobProcessor implements PageProcessor {

    private String url =
            "https://search.51job.com/list/080200,000000,0000,01,9,99,%25E6%259C%25BA%25E5%2599%25A8%25E5%25AD%25A6%25E4%25B9%25A0,2,1.html?lang=c&stype=&postchannel=0000&workyear=99&cotype=99&degreefrom=99&jobterm=99&companysize=99&providesalary=99&lonlat=0%2C0&radius=-1&ord_field=0&confirmdate=9&fromType=&dibiaoid=0&address=&line=&specialarea=00&from=&welfare=";

    private Site site = Site.me()
            .setCharset("gbk")
            .setTimeOut(10 * 1000)
            .setRetrySleepTime(3000)
            .setRetryTimes(3);

    @Override
    public void process(Page page) {
        List<Selectable> list = page.getHtml().css("div#resultList div.el").nodes();
        // 判断获取到的集合是否为空
        if (list.size() == 0) {
            // 如果为空，表示这是招聘详情页，解析页面，获取招聘的详细信息，保存数据
            this.parseJobInfoPage(page);
        } else {
            // 如果不为空，表示这是列表页，解析出详情页的url地址，放到任务队列中
            for (Selectable selectable : list) {
                String jobInfoUrl = selectable.links().toString();
                page.addTargetRequest(jobInfoUrl);
            }
            // 获取下一页的rul
            String nextPageUrl = page.getHtml().css("div.p_in li.bk").nodes().get(1).links().toString();
            page.addTargetRequest(nextPageUrl);
        }

    }

    /**
     * 解析页面，获取招聘的详细信息，保存数据
     *
     * @param page
     */
    private void parseJobInfoPage(Page page) {
        JobInfo jobInfo = new JobInfo();

        Html html = page.getHtml();


        // 封装对象
        jobInfo.setUrl(page.getUrl().toString());
        jobInfo.setCompanyName(html.css("div.cn p.cname a", "text").toString().trim());
        jobInfo.setCompanyAddr(Jsoup.parse(html.css("div.bmsg").nodes().get(1).toString()).text());
        jobInfo.setCompanyInfo(html.css("div.tmsg", "text").toString().trim());
        jobInfo.setJobName(html.css("div.cn h1", "text").toString().trim());

        String[] ltype = html.css("div.cn p.ltype", "title").toString().replace(String.valueOf((char) 160), "").split("\\|");
        jobInfo.setJobAddr(ltype[0].trim());
        jobInfo.setJobInfo(Jsoup.parse(html.css("div.job_msg").toString()).text().trim());

        Integer[] salary = MathSalary.getSalary(html.css("div.cn strong", "text").toString());
        jobInfo.setSalaryMin(salary[0]);
        jobInfo.setSalaryMax(salary[1]);
        jobInfo.setTime(html.css("div.cn p.ltype", "title").regex("(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))").toString());

        // 保存数据
        page.putField("jobInfo", jobInfo);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Autowired
    private SpringDataPipline springDataPipline;

    // initialDelay:当任务启动后，等待多久执行任务
    // fixedDelay: 每隔多久执行任务
    @Scheduled(initialDelay = 1000, fixedDelay = 100 * 1000)
    public void main() {
        Spider.create(new JobProcessor())
                .addUrl(url)
                .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(100000)))
                .thread(10)
                .addPipeline(this.springDataPipline)
                .run();
    }
}
