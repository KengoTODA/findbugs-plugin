package jp.co.worksap.oss.findbugs.lombok;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
class ProperParentValue {
    private ProperChildValue child;
    private ClassWithoutToStringMethod anotherField;
}
