package com.loit.base.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.loit.base.annotation.TableName;
import com.loit.base.entity.AbstractBaseEntity;
import com.loit.base.entity.RequestPage;
import com.loit.base.mapper.BaseMapper;
import com.loit.base.utils.ThreadLocalUtils;
import com.loit.modules.sys.utils.UserUtils;
import com.rabbitmq.client.impl.AMQImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/8   9:01
 * @Description: 抽象service基类
 */
public class AbstractBaseService<TEntity extends AbstractBaseEntity,TMapper extends BaseMapper> {

    public Logger logger = LoggerFactory.getLogger(getClass());
    
    @Resource(name = "dataSource")
    private DruidDataSource dataSource;

    @Autowired
    private TMapper tMapper;

    public TEntity findOne(String id){
        try {
           ThreadLocalUtils.set(this.getEntity());
           Assert.notNull(id,"id不能为空");
           return (TEntity)tMapper.findOne(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    /**
     * 获取所有数据
     * @return 所有数据
     */
    public List<TEntity> findAll(){
        try {
            ThreadLocalUtils.set(this.getEntity());
            return tMapper.findAll(this.getEntity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    /**
     * 分页获取数据
     * @param page 分页信息
     * @return 分页数据
     */
    public RequestPage findAll(RequestPage page){
        try {
            ThreadLocalUtils.set(this.getEntity());
            List<TEntity> list = tMapper.findPage(page);
            Long count = this.findCount(this.getTableName());
            page.setList(list);
            page.setTotal(count);
            ThreadLocalUtils.remove();
            return page;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    /**
     * 保存方法
     * @param tEntity 实体对象
     * @return 实体对象
     */
    @Transactional(propagation= Propagation.NOT_SUPPORTED)
    public TEntity save(TEntity tEntity){
        String userId = UserUtils.getUser().getId();
        Date date = new Date();
        if(StringUtils.isBlank(tEntity.getId())){
            tEntity.setId(UUID.randomUUID().toString());
        }
        tEntity.setCreator(userId);
        tEntity.setCreateTime(date);
        tEntity.setCreator(userId);
        tEntity.setCreateTime(date);
        tEntity.setTs(date);
        tEntity.setDr(0);
        tMapper.save(tEntity);
        return tEntity;
    }

    /**
     * 更新方法
     * @param tEntity 实体对象
     * @return 实体对象
     */
    @Transactional(propagation= Propagation.NOT_SUPPORTED)
    public TEntity update(TEntity tEntity){
        Assert.notNull(tEntity.getId(),"id不能为空");
        String userId = UserUtils.getUser().getId();
        Date date = new Date();
        tEntity.setModifier(userId);
        tEntity.setCreateTime(date);
        tEntity.setTs(date);
        tMapper.update(tEntity);
        return tEntity;
    }

    /**
     * 根据id逻辑删除
     * 必须严格按照命名规范进行，否则无法解析字节码对象
     * @param id 主键
     */
    @Transactional(propagation= Propagation.NOT_SUPPORTED)
    public void delete(String id)  {
        Assert.notNull(id,"id不能为空");
        String tableName = this.getTableName();
        Assert.notNull(tableName,"未获取到数据库表名");
        tMapper.delete(tableName+"--"+id);
    }

    /**
     * 根据ids字符串批量逻辑删除
     * 必须严格按照命名规范进行，否则无法解析字节码对象
     * @param ids 主键字符串（1,2,3.........）
     */
    @Transactional(propagation= Propagation.NOT_SUPPORTED)
    public void deleteAll(String ids) {
        Assert.notNull(ids,"ids不能为空");
        String tableName = this.getTableName();
        Assert.notNull(tableName,"未获取到数据库表名");
        tMapper.deleteAll(tableName+"--"+ids);
    }

    /**
     * 根据Mapper类动态获取数据库表面
     * @return  Entity类字节码对象
     */
    private String getTableName() {
        String tableName = null;
        String entityPackage = null;
        try {
            Type[] types =  tMapper.getClass().getGenericInterfaces();
            if(types != null && types.length >0){
                for (Type type : types) {
                    String name = ((Class) type).getName();
                    entityPackage = name.replace("Mapper","").replace("mapper","entity");
                }
            }
            Class<?> clazz = Class.forName(entityPackage);
            tableName = clazz.getAnnotation(TableName.class).name();
        } catch (ClassNotFoundException e) {
            logger.error(String.format("根据【%s】未找到指定Entity类",entityPackage));
        }  catch (Exception e){
            logger.error("未获取到真实Mapper类");
        }
        return tableName;
    }

    /**
     * 根据Mapper获取Entity实体字节码对象
     * @return Entity实体字节码对象
     */
    private Class<? extends TEntity> getEntity(){
        String entityPackage = null;
        Class<? extends TEntity> clazz = null;
        Type[] types =  tMapper.getClass().getGenericInterfaces();
        if(types != null && types.length >0){
            for (Type type : types) {
                String name = ((Class) type).getName();
                entityPackage = name.replace("Mapper","").replace("mapper","entity");
            }
        }
        try {
            clazz = (Class<? extends TEntity>) Class.forName(entityPackage);
        } catch (ClassNotFoundException e) {
            logger.error("未找到指定class类",e);
        }
        return clazz;
    }

    /**
     * 获取分页总条数
     * @param tableName 数据库表名称
     * @return 总条数
     */
    private Long findCount(String tableName)  {
        String countSql = "";
        long count = 0L;
        try {
            Connection connection = dataSource.getConnection();
            if(connection != null){
                countSql = String.format("select count(id) from %s force index (PRIMARY)",tableName);
                PreparedStatement preparedStatement = connection.prepareStatement(countSql);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    count =  resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error(String.format("SQL异常【%s】",countSql),e);
        }
        return count;
    }
}
