package org.leocoder.picture.domain.vo.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-31 15:26
 * @description :
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
