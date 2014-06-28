package jp.co.worksap.oss.findbugs.lombok;

import lombok.Data;
import lombok.ToString;

@ToString(exclude = "parent")
@Data
class ProperChildValue {
    private ProperParentValue parent;
}
