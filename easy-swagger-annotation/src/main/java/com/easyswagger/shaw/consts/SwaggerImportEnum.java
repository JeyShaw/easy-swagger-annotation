package com.easyswagger.shaw.consts;

/**
 * @author JShaw
 */
public enum SwaggerImportEnum {

    /**
     * 实体类注解
     */
    ApiModel("ApiModel", "io.swagger.annotations", 0),

    /**
     * 实体字段注解
     */
    ApiModelProperty("ApiModelProperty", "io.swagger.annotations", 1),

    /**
     * 控制器类注解
     */
    Api("Api", "io.swagger.annotations", 0),

    /**
     * 控制器方法注解
     */
    ApiOperation("ApiOperation", "io.swagger.annotations", 2);

    /**
     * 简单类名
     */
    private String clazz;

    /**
     * 包名
     */
    private String pkg;

    /**
     * 0 类， 1 字段， 2，方法
     */
    private Integer type;

    private SwaggerImportEnum(String clazz, String pkg, Integer type) {
        this.clazz = clazz;
        this.pkg = pkg;
        this.type = type;
    }

    public String getClazz() {
        return this.clazz;
    }

    public String getPkg() {
        return this.pkg;
    }

    public Integer getType() {
        return this.type;
    }

    public String getFullName() {
        return this.pkg + "." + this.clazz;
    }
}
