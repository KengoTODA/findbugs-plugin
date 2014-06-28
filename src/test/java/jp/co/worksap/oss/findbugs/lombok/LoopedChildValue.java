package jp.co.worksap.oss.findbugs.lombok;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
class LoopedChildValue {
    private LoopedParentValue parent;
}
