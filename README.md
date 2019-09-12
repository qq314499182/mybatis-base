
## 数据库表设计
数据库表在设计中必须带有如下字段，此为mysql数据，如使用oracle数据库请替换对应数据类型

字段|类型|说明
:-:|:-:|:-:
id | varchar(40)|主键
creator| varchar(40)|创建人
create_time  |datetime|创建时间
modifier|varchar(40)|修改人
modified_time|datetime|修改时间
ts|datetime|事件戳
dr|varchar(1)|删除标记

## 代码部分
### 一、实体类相关
1、实体类继承 AbstractBaseEntity基类
2、类注解 @TableName(name = " ")  name中填写数据库表名
3、属性注解 @Exclude 该属性不参与持久化
4、实体类全类名必须为 aaa.aaa.entity.Aaaa 格式，同时全类名中只允许出现一次entity包名

示例代码：
```java
package *.*.demon.entity;

import com.*.base.annotation.Exclude;
import com.*.base.annotation.TableName;
import com.*.base.entity.AbstractBaseEntity;

@TableName(name = "demon")
public class Demon extends AbstractBaseEntity {

    private String code;

    private String name;

    private String sex;

    private Integer age;

    @Exclude
    private String option;
```
### 二、service类与mapper类相关
1、service类继承AbstractBaseService基类
2、mapper接口继承BaseMapper接口
3、mapper全类名必须为 aaa.aaa.mapper.AaaaMapper格式，同时全类名中只允许出现一次mapper包名

实例代码
```java
@Service
public class DemoService extends AbstractBaseService<Demo, DemoMapper> {
}
```
```java
@MyBatisDao
public interface DemoMapper extends BaseMapper<Demo> {
}
```
### 三、调用API相关实例
在controller层调用service基类方法，无XML实现单表基本操作。
```java
package com.*.demo.web;

import com.*.base.entity.RequestPage;
import com.*.demo.entity.Demo;
import com.*.demo.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("demoWeb")
public class DemoWeb {

    @Autowired
    private DemoService demoService;

    @RequestMapping("add")
    public Demo add(){
        Demo demo = new Demo();
        demo.setCode("123");
        demo.setName("测试");
        demo.setSex("男");
        demo.setAge(12);
        return demoService.save(demo);
    }

    @RequestMapping("update")
    public Demo update(){
        Demo demo = new Demo();
        demo.setId("7bb977db-6934-44fe-9162-0614f1d3393e");
        demo.setCode("123修改");
        demo.setName("测试修改");
        demo.setSex("女");
        demo.setAge(20);
        return demoService.update(demo);
    }

    @RequestMapping("findOne")
    public Demo findOne(){return demoService.findOne("7bb977db-6934-44fe-9162-0614f1d3393e");}

    @RequestMapping("findAll")
    public List<Demo> findAll(){return demoService.findAll();}

    @RequestMapping("findPage")
    public RequestPage findPage(){return demoService.findAll(new RequestPage(1,15));}

    @RequestMapping("delete")
    public void delete(){demoService.delete("4f1d7c0d");}

    @RequestMapping("delAll")
    public void delAll(){demoService.deleteAll("78aa00b5,7ca25f3a,913c0c52");}
}

```
### 四、未完成部分
- 添加自定义数据库字段名称注解
- 查询方法添加排序与条件查询
- 编写BaseController基类，真正实现后台无代码
- 主子表的级联保存，单条数据主子的级联查询
- 编写前端模版代码，配合后台，实现自动生成网页的目的
- 分页总条数查询优化

### 五、鸣谢
此框架是基于mybatis的@InsertProvider @UpdateProvider @SelectProvider 与 Interceptor拦截器基础上实现的，在这过程中遇到查询结果封装问题，特鸣谢 @ [YingTao8](https://blog.csdn.net/yingtao8)    @[weixin_34228387](https://blog.csdn.net/weixin_34228387)   两位文章的帮助

 [mybatis 拓展 -- 通用mapper 和 动态 resultMap](https://blog.csdn.net/weixin_34228387/article/details/88762624)
 [mybatis抽取基类BaseMapper(通用增/删/改/查)](https://blog.csdn.net/YingTao8/article/details/83116256)
 本文实例代码及具体实现代码已上传 [github](https://blog.csdn.net/weixin_34228387/article/details/88762624)有兴趣的同学欢迎交流学习
