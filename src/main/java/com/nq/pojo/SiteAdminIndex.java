package com.nq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName site_admin_index
 */
@TableName(value ="site_admin_index")
@Data
public class SiteAdminIndex implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String colorWeak;

    /**
     * 
     */
    private String contentWidth;

    /**
     * 
     */
    private String fixSiderbar;

    /**
     * 
     */
    private String fixedHeader;

    /**
     * 
     */
    private String hideCopyButton;

    /**
     * 
     */
    private String hideHintAlert;

    /**
     * 
     */
    private String layout;

    /**
     * 
     */
    private String primaryColor;

    /**
     * 
     */
    private String theme;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}