package jp.co.worksap.oss.findbugs.lombok;

import java.util.Map;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class LoopedParentValueWithMap {
    private Map<String, LoopedChildValue> children;
}
