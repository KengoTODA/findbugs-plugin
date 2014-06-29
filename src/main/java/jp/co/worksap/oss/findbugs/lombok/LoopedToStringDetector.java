package jp.co.worksap.oss.findbugs.lombok;

import org.apache.bcel.classfile.Method;

import com.google.common.base.Optional;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class LoopedToStringDetector extends BytecodeScanningDetector {

    private final BugReporter bugReporter;

    public LoopedToStringDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visitMethod(Method method) {
        super.visitMethod(method);
        if (method.isPublic() && "toString".equals(method.getName()) && "()Ljava/lang/String;".equals(method.getSignature())) {
            // this is #toString method, dig this to research reference

            Optional<String> errorReport = new CyclicMethodReferenceFinder().find(getClassContext().getJavaClass(), getMethodDescriptor());
            if (errorReport.isPresent()) {
                bugReporter.reportBug(new BugInstance(this, "LOMBOK_CYCLIC_TO_STRING", HIGH_PRIORITY)
                    .addClassAndMethod(this)
                    .addString(errorReport.get()));
            }
        }
    }
}
