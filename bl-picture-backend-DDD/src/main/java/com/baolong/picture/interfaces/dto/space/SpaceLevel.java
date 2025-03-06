package com.baolong.picture.interfaces.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间等级类
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

	private int value;

	private String text;

	private long maxCount;

	private long maxSize;
}
