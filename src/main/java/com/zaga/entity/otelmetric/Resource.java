package com.zaga.entity.otelmetric;

import com.zaga.entity.otellog.resource.Attributes;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import lombok.Data;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private Attributes attributes; 
}