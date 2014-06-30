package jp.co.worksap.oss.findbugs.lombok;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
class ProperParentValueWithChildren {
    private Collection<ProperChildValue> children;
    private Map<String, ProperChildValue> anotherField;
    private ArrayList<ProperChildValue> arrayList;
}
