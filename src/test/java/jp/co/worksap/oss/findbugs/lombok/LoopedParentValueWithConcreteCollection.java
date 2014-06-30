package jp.co.worksap.oss.findbugs.lombok;

import java.util.ArrayList;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class LoopedParentValueWithConcreteCollection {
    private ArrayList<LoopedChildValue> children;
}
