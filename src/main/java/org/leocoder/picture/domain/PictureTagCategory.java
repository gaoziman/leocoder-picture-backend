package org.leocoder.picture.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : 程序员Leo
 * @version 1.0
 * @date 2024-12-16 11:34
 * @description :
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureTagCategory {
    private List<String> tagList;

    private List<String> categoryList;
}
