package com.loit.base.mapper;

import com.loit.base.entity.AbstractBaseEntity;
import com.loit.base.entity.RequestPage;
import com.loit.base.provider.BaseSqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.nutz.mvc.annotation.Param;

import java.util.List;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/7 0007 16:41
 * @Description: 抽象Mapper基类
 */
public interface BaseMapper<TEntity extends AbstractBaseEntity> {

    /**
     * 新增数据
     * @param tEntity 实体
     * @return 成功返回实体
     */
    @InsertProvider(method = "save",type= BaseSqlProvider.class)
    Integer save(TEntity tEntity);

    /**
     *  逻辑删除
     * @param params 表面-主键
     */
    @UpdateProvider(method = "delete",type =BaseSqlProvider.class )
    void delete(String params);

    /**
     * 逻辑删除
     * @param params 主键-主键字符串
     */
    @UpdateProvider(method = "deleteAll",type =BaseSqlProvider.class )
    void deleteAll(String params);

    /**
     * 更新数据
     * @param tEntity 实体
     */
    @UpdateProvider(method = "update",type = BaseSqlProvider.class)
    void update(TEntity tEntity);

    /**
     * 获取所有数据
     * @return 所有数据
     */
    @SelectProvider(method = "findAll",type = BaseSqlProvider.class)
     List<TEntity> findAll(Class<TEntity> clazz);

    /**
     * 分页获取数据
     * @param page 分页参数
     * @return 分页数据
     */
    @SelectProvider(method = "findPage",type = BaseSqlProvider.class)
     List<TEntity> findPage(RequestPage page);

    /**
     * 根据主键获取实体
     * @param id 主键
     * @return 实体
     */
    @SelectProvider(method = "findOne",type = BaseSqlProvider.class)
     TEntity findOne(String id);
}
