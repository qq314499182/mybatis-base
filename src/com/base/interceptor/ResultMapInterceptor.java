package com.loit.base.interceptor;

import com.loit.base.entity.TableMataDate;
import com.loit.base.utils.ThreadLocalUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/26 0026  9:27
 * @Description: 抽象基类查询结果封装拦截器
 */

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class ResultMapInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        if(ms.getResource().contains(".xml")){
            return invocation.proceed();
        }
        ResultMap resultMap = ms.getResultMaps().iterator().next();
        if (CollectionUtils.isNotEmpty(resultMap.getResultMappings())) {
            return invocation.proceed();
        }
        Class clazz = (Class)ThreadLocalUtils.get();
        TableMataDate mataDate = TableMataDate.forClass(clazz);
        Map<String, Class<?>> fieldTypeMap = mataDate.getFieldTypeMap();
        List<ResultMapping> resultMappings = new ArrayList<>(fieldTypeMap.size());
        for (Map.Entry<String, String> entry : mataDate.getFieldColumnMap().entrySet()) {
            ResultMapping resultMapping = new ResultMapping.Builder(ms.getConfiguration(), entry.getKey(), entry.getValue(), fieldTypeMap.get(entry.getKey())).build();
            resultMappings.add(resultMapping);
        }
        ResultMap newRm = new ResultMap.Builder(ms.getConfiguration(), resultMap.getId(), clazz, resultMappings).build();
        Field field = ReflectionUtils.findField(MappedStatement.class, "resultMaps");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, ms, Collections.singletonList(newRm));
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
