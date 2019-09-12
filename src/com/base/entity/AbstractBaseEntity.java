package com.loit.base.entity;

import com.loit.base.annotation.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/7 0007 15:17
 * @Description: 抽象entity基类
 */
public class AbstractBaseEntity implements Serializable {

    @Exclude
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    protected String id;

    /**
     * 创建人
     */
    protected String creator;

    /**
     * 创建时间
     */
    protected Date createTime;

    /**
     * 修改人
     */
    protected String modifier;

    /**
     * 修改事件
     */
    protected String modifiedTime;

    /**
     * 删除标识
     */
    protected Integer dr;

    /**
     * 时间戳
     */
    protected Date ts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getDr() {
        return dr;
    }

    public void setDr(Integer dr) {
        this.dr = dr;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }
}
