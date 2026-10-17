package com.zhaoyushi.types.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhaoy
 * @date 2026/10/14
 * @description Response
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> implements Serializable {

    private String code;
    private String info;
    private T data;

}
