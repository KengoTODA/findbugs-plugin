package jp.co.worksap.oss.findbugs.lombok;

import java.util.Collection;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class LoopedParentValueWithChildren {
    private Collection<LoopedChildValue> children;
}
