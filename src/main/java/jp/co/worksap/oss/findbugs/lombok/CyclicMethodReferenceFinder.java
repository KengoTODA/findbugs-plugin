package jp.co.worksap.oss.findbugs.lombok;

import java.util.Set;

import jp.co.worksap.oss.findbugs.common.VisitedMethodFinder;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.classfile.MethodDescriptor;

class CyclicMethodReferenceFinder {
    private static final Joiner ARROW_JOINER = Joiner.on(" -> ");

    private final Set<MethodDescriptor> visitedMethod;

    CyclicMethodReferenceFinder() {
        this.visitedMethod = Sets.newHashSet();
    }

    CyclicMethodReferenceFinder(Set<MethodDescriptor> method) {
        this.visitedMethod = Sets.newHashSet(method);
    }

    Optional<String> find(String className, MethodDescriptor methodDescriptor) {
        for (MethodDescriptor calledMethod : VisitedMethodFinder
                .listVisitedMethodFrom(className, methodDescriptor)) {
            String calledClassName = calledMethod.getClassDescriptor().getDottedClassName();
            if (calledClassName.startsWith("java.lang.")) {
                // optimization: we do not need to dig system classes
                continue;
            }

            if (visitedMethod.contains(calledMethod)) {
                return Optional.of(stringfy(calledMethod));
            }
            visitedMethod.add(calledMethod);

            Optional<String> errorReport = new CyclicMethodReferenceFinder(visitedMethod)
                    .find(calledClassName, calledMethod);
            if (errorReport.isPresent()) {
                return Optional.of(ARROW_JOINER.join(
                        stringfy(calledMethod),
                        errorReport.get()));
            }
        }

        return Optional.absent();
    }

    private String stringfy(MethodDescriptor method) {
        return String.format("%s#%s%s", method.getClassDescriptor().getDottedClassName(), method.getName(), method.getSignature());
    }
}
