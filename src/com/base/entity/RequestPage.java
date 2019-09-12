package com.loit.base.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: zhaoke
 * @Date: 2019/9/2 0002  9:10
 * @Description: 分页实体
 */
public class RequestPage<TEntity extends AbstractBaseEntity> implements Serializable {

    private static final long serialVersionUID = 7291867458565557623L;

    private static final Pattern NUMBER = Pattern.compile("^([1-9][0-9]*)$");

    public RequestPage() {
    }

    public RequestPage(HttpServletRequest request){
        //分页校验
        this.checkPage(request);
        this.pageNum = Integer.parseInt(request.getParameter("pageNum"));
        this.pageSize = Integer.parseInt(request.getParameter("pageSize"));
    }

    public RequestPage(Integer pageNum,Integer pageSize){
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 显示条数
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Long totalNum;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 分页数据
     */
    private List<TEntity> list ;


    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<TEntity> getList() {
        return list;
    }

    public void setList(List<TEntity> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
        this.totalNum = total/pageSize;
    }

    public Long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Long totalNum) {
        this.totalNum = totalNum;
    }

    /**
     * 校验分页参数
     * @param request 请求
     */
    private void checkPage(HttpServletRequest request) {
        if(!"GET".equals(request.getMethod())){
            throw new RuntimeException("分页请求方式错误，请使用GET方式请求");
        }
        if(!request.getQueryString().contains("pageNum") ){
            throw new RuntimeException("分页请求参数错误，请使用pageNum(当前页数)来传递分页信息");
        }
        if( !request.getQueryString().contains("pageSize")){
            throw new RuntimeException("分页请求参数错误，请使用pageSize(显示条数) 来传递分页信息");
        }
        String pageNum = request.getParameter("pageNum");
        String pageSize = request.getParameter("pageSize");
        if(StringUtils.isBlank(pageNum)){
            throw new RuntimeException("分页请求参数错误，pageNum(当前页数) 不能为空");
        }
        if(StringUtils.isBlank(pageSize)){
            throw new RuntimeException("分页请求参数错误，pageSize(显示条数) 不能为空");
        }
        Matcher pageNumMatcher = NUMBER.matcher(pageNum);
        if(!pageNumMatcher.find()){
            throw new RuntimeException("分页请求参数错误，pageNum(当前页数) 必须为正整数");
        }
        Matcher pageSizeMatcher = NUMBER.matcher(pageSize);
        if(!pageSizeMatcher.find()){
            throw new RuntimeException("分页请求参数错误，pageSize(显示条数) 必须为正整数");
        }
    }
}
