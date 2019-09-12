package com.loit.base.entity;

import com.loit.base.annotation.Exclude;
import com.loit.base.annotation.TableName;
import org.apache.shiro.crypto.hash.Hash;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.persistence.Transient;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/26 0026  10:28
 * @Description: 抽象基类元数据信息（数据库与实体关系）
 */
public class TableMataDate {

    //缓存元数据
    private static final Map<Class<?>,TableMataDate> TABLE_CACHE = new ConcurrentHashMap<>(64);

    private static final Pattern LINE_PATTERN = Pattern.compile("_(\\w)");

    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    private TableMataDate(Class<?> clazz) {
        this.fieldColumnMap = new HashMap<>();
        this.fieldTypeMap = new HashMap<>();
        initTableInfo(clazz);
    }

    /**
     * 表名
     */
    private String tableName;

    /**
     * 主键属性名
     */
    private String pkProperty;

    /**
     * 主键对应的列名
     */
    private String pkColumn;

    /**
     * 属性名和字段名映射关系的 map
     */
    private Map<String, String> fieldColumnMap;

    /**
     * 字段类型
     */
    private Map<String, Class<?>> fieldTypeMap;


    public static TableMataDate forClass(Class<?> entityClass) {
        TableMataDate tableMataDate = TABLE_CACHE.get(entityClass);
        if (tableMataDate == null) {
            tableMataDate = new TableMataDate(entityClass);
            TABLE_CACHE.put(entityClass, tableMataDate);
        }
        return tableMataDate;
    }

    public String getBaseColumns() {
        Collection<String> columns = fieldColumnMap.values();
        if (CollectionUtils.isEmpty(columns)) {
            throw new RuntimeException("未解析到字段信息");
        }
        Iterator<String> iterator = columns.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String next = iterator.next();
            sb.append(next);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    private void initTableInfo(Class<?> clazz) {
        boolean annotation = clazz.isAnnotationPresent(TableName.class);
        Assert.isTrue(annotation,"实体未添加 com.loit.base.annotation @TableName注解，无法解析实体与数据表关系");
        this.tableName = clazz.getAnnotation(TableName.class).name();
        Set<Field> fields = this.getFields(clazz,new HashSet<Field>());
        for (Field field : fields) {
            if(Modifier.isStatic(field.getModifiers()) || !BeanUtils.isSimpleValueType(field.getType())){
                continue;
            }
            String property = field.getName();
            // 将字段对应的列放到 map 中
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, property);
            if (descriptor != null && descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null) {
                fieldColumnMap.put(property, this.humpToLine(property));
                fieldTypeMap.put(property, field.getType());
            }
        }
    }

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPkProperty() {
        return pkProperty;
    }

    public void setPkProperty(String pkProperty) {
        this.pkProperty = pkProperty;
    }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public Map<String, String> getFieldColumnMap() {
        return fieldColumnMap;
    }

    public void setFieldColumnMap(Map<String, String> fieldColumnMap) {
        this.fieldColumnMap = fieldColumnMap;
    }

    public Map<String, Class<?>> getFieldTypeMap() {
        return fieldTypeMap;
    }

    public void setFieldTypeMap(Map<String, Class<?>> fieldTypeMap) {
        this.fieldTypeMap = fieldTypeMap;
    }
}
