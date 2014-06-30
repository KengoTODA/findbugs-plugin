package jp.co.worksap.oss.findbugs.lombok;

import org.junit.Test;

import com.youdevise.fbplugins.tdd4fb.DetectorAssert;

import edu.umd.cs.findbugs.BugReporter;

public class LoopedToStringDetectorTest {
    @Test
    public void testLoopedParent() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertBugReported(LoopedParentValue.class, detector, bugReporter);
    }

    @Test
    public void testLoopedChild() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertBugReported(LoopedChildValue.class, detector, bugReporter);
    }

    @Test
    public void testProperParent() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertNoBugsReported(ProperParentValue.class, detector, bugReporter);
    }

    @Test
    public void testProperChild() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertNoBugsReported(ProperChildValue.class, detector, bugReporter);
    }

    @Test
    public void testLoopedParentWithChildren() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertBugReported(LoopedParentValueWithChildren.class, detector, bugReporter);
    }

    @Test
    public void testProperParentWithChildren() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertNoBugsReported(ProperParentValueWithChildren.class, detector, bugReporter);
    }

    @Test
    public void testTriangleReference() throws Exception {
        BugReporter bugReporter = DetectorAssert.bugReporterForTesting();
        LoopedToStringDetector detector = new LoopedToStringDetector(bugReporter);

        DetectorAssert.assertBugReported(TriangleReferenceA.class, detector, bugReporter);
    }
}
