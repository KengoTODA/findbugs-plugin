package jp.co.worksap.oss.findbugs.lombok;

import java.util.Map;

import jp.co.worksap.oss.findbugs.common.VisitedMethodFinder;

import org.apache.bcel.classfile.JavaClass;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.umd.cs.findbugs.classfile.MethodDescriptor;

class CyclicMethodReferenceFinder {
    private static final Joiner ARROW_JOINER = Joiner.on(" -> ");

    private final Multimap<JavaClass, MethodDescriptor> visitedMethod;

    CyclicMethodReferenceFinder() {
        this.visitedMethod = HashMultimap.create();
    }

    CyclicMethodReferenceFinder(Multimap<JavaClass, MethodDescriptor> method) {
        this.visitedMethod = HashMultimap.create(method);
    }

    Optional<String> find(JavaClass javaClass, MethodDescriptor methodDescriptor) {
        for (Map.Entry<JavaClass, MethodDescriptor> called : VisitedMethodFinder
                .listVisitedMethodFrom(javaClass, methodDescriptor)
                .entries()) {
            JavaClass calledClass = called.getKey();
            MethodDescriptor calledMethod = called.getValue();
            if (visitedMethod.containsEntry(calledClass, calledMethod)) {
                return Optional.of(stringfy(called));
            }
            visitedMethod.put(calledClass, calledMethod);
            if (calledClass.isNative() ||
                    calledClass.getClassName().equals("java.lang.System") ||
                    calledClass.getClassName().equals("java.lang.String") ||
                    calledClass.getClassName().equals("java.lang.StringBuilder") ||
                    calledClass.getClassName().equals("java.lang.Arrays")) {
                // optimization: we do not need to dig system classes
                continue;
            }

            Optional<String> errorReport = new CyclicMethodReferenceFinder(visitedMethod)
                    .find(calledClass, calledMethod);
            if (errorReport.isPresent()) {
                return Optional.of(ARROW_JOINER.join(
                        stringfy(called),
                        errorReport.get()));
            }
        }

        return Optional.absent();
    }

    private String stringfy(Map.Entry<JavaClass, MethodDescriptor> entry) {
        return String.format("%s#%s%s", entry.getKey().getClassName(), entry.getValue().getName(), entry.getValue().getSignature());
    }
}
