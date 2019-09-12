package com.loit.base.provider;

import com.loit.base.annotation.Exclude;
import com.loit.base.annotation.TableName;
import com.loit.base.entity.AbstractBaseEntity;
import com.loit.base.entity.RequestPage;
import com.loit.base.entity.TableMataDate;
import com.loit.base.utils.ThreadLocalUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/7 0007  16:11
 * @Description: 动态拼接sql处理类
 */
public class BaseSqlProvider<TEntity extends AbstractBaseEntity> {

    private static final Pattern LINE_PATTERN = Pattern.compile("_(\\w)");

    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    public String findOne(String id){
        Class clazz = (Class)ThreadLocalUtils.get();
        TableMataDate tableMataDate = TableMataDate.forClass(clazz);
        String tableName = tableMataDate.getTableName();
        SQL sql = new SQL();
        sql = sql.SELECT(tableMataDate.getBaseColumns()).FROM(tableName).WHERE(String.format("id = '%s' ", id));
        System.out.println(sql.toString());
        return sql.toString();
    }

    /**
     * 动态生成查询sql（获取全部数据）
     * @return 查询sql
     */
    public String findAll(Class<? extends TEntity> clazz){
        SQL sql = new SQL();
        TableMataDate tableMataDate = TableMataDate.forClass(clazz);
        sql.SELECT(tableMataDate.getBaseColumns());
        sql.FROM(tableMataDate.getTableName());
        return sql.toString();
    }

    /**
     * 动态生成分页查询sql
     * @param page 分页参数
     * @return 分页查询sql
     */
    public String findPage(RequestPage page){
        SQL sql = new SQL();
        Class clazz = (Class)ThreadLocalUtils.get();
        TableMataDate tableMataDate = TableMataDate.forClass(clazz);
        sql.SELECT(tableMataDate.getBaseColumns());
        String tableName = tableMataDate.getTableName();
        String join = sql.FROM(tableMataDate.getTableName()).toString();
        StringBuilder builder = new StringBuilder(join);
        builder.append(" join (")
                    .append(" select id as pid from ")
                    .append(tableName)
                    .append(String.format(" limit %s,%s",(page.getPageNum()-1)*page.getPageSize(),page.getPageSize()))
                    .append(" ) as page")
                    .append(" on page.pid = ")
                    .append(tableName)
                    .append(".id");
        return builder.toString();
    }

    /**
     *  动态生成新增sql语句
     * @param tEntity 实体对象
     * @return 新增sql语句
     */
    public String save(TEntity tEntity){
        SQL sql = new SQL();
        Class<?> cClass = tEntity.getClass();
        TableName tableName = cClass.getAnnotation(TableName.class);
        String name = tableName.name();
        sql.INSERT_INTO(name);
        Set<Field> fields = this.getFields(cClass,new HashSet<Field>());
        if(CollectionUtils.isNotEmpty(fields)){
            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers()) || !BeanUtils.isSimpleValueType(field.getType())){
                    continue;
                }
                field.setAccessible(true);
                String column = field.getName();
                sql.VALUES(this.humpToLine(column), "#{" + column + "}");
            }
        }
        return sql.toString();
    }

    /**
     * 动态生产更新sql语句
     * @param tEntity 实体对象
     * @return  String 更新sql语句
     */
    public String update(TEntity tEntity){
        SQL sql = new SQL();
        Class cClass = tEntity.getClass();
        TableName tableName = (TableName)cClass.getAnnotation(TableName.class);
        String name = tableName.name();
        Set<Field> fields = this.getFields(cClass,new HashSet<Field>());
        if(CollectionUtils.isNotEmpty(fields)){
            sql.UPDATE(name);
            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers()) || !BeanUtils.isSimpleValueType(field.getType())){
                    continue;
                }
                field.setAccessible(true);
                String column = field.getName();
                if(column.equals("id")){
                    continue;
                }
                //过滤字段值为空
                try {
                    Object value = field.get(tEntity);
                    if(value == null){
                        continue;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("获取实体对象属性值失败",e);
                }
                sql.SET(this.humpToLine(column) + "=" +"#{" + column + "}");
            }
            String id = tEntity.getId();
            Assert.notNull(id,"实体主键不能为空");
            sql.WHERE(String.format("id = '%s' ",id));
        }
        return sql.toString();
    }

    /**
     * 根据id逻辑删除
     * @param params 表面-主键
     * @return 逻辑删除sql语句
     */
    public String delete(String params){
        String[] paramsArray = params.split("--");
        SQL sql = new SQL();
        sql.UPDATE(paramsArray[0]);
        sql.SET("dr = 1");
        sql.WHERE( String.format("id = '%s' ",paramsArray[1]));
        return sql.toString();
    }

    /**
     * 根据ids串批量逻辑删除
     * @param params 表面-主键字符串
     * @return 批量逻辑删除sql语句
     */
    public String deleteAll(String params){
        String[] paramsArray = params.split("--");
        SQL sql = new SQL();
        sql.UPDATE(paramsArray[0]);
        sql.SET("dr = 1");
        String[] idArray = paramsArray[1].split(",");
        StringBuilder builder = new StringBuilder();
        for (String id : idArray) {
            builder.append(String.format("'%s',",id));
        }
        if(builder.length() > 0){
            builder.deleteCharAt(builder.length() - 1);
        }
        sql.WHERE(String.format("id in ( %s )",builder.toString()));
        return sql.toString();
    }

    /**
     * 获取对象中字段
     * @param clazz 字节码对象
     * @return 属性集合
     */
    private Set<Field> getFields(Class clazz, Set<Field> fieldList){
        Field[] fields = clazz.getDeclaredFields();
        if(fields.length >0){
            for (Field field : fields) {
                field.setAccessible(true);
                Exclude key = field.getAnnotation(Exclude.class);
                if (key == null) {
                    fieldList.add(field);
                }
            }
        }
        //递归获取父类属性
        Class superclass = clazz.getSuperclass();
        if(superclass != null && superclass != Object.class){
            fieldList.addAll(this.getFields(superclass,fieldList));
        }
        return fieldList;
    }

    /**
     * sql字段下划线转对象属性驼峰
     * @param str sql字段
     * @return  对象属性驼峰
     */
    private String lineToHump(String str){
        str = str.toLowerCase();
        Matcher matcher = LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 对象属性驼峰转sql字段下划线
     * @param str  对象属性驼峰
     * @return sql字段下划线
     */
    private String humpToLine(String str) {
        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
