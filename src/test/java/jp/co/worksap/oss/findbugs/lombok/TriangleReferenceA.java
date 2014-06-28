package jp.co.worksap.oss.findbugs.lombok;

import lombok.ToString;

@ToString
public class TriangleReferenceA {
    private TriangleReferenceB next;
}
