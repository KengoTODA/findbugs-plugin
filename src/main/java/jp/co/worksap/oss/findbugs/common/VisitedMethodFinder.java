package jp.co.worksap.oss.findbugs.common;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.umd.cs.findbugs.bcel.BCELUtil;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * <p>Simple ClassVisitor implementation to find visited methods in the specified method.</p>
 * <p>To create instance, you need to provide name and descriptor to specify the target method.</p>
 *
 * @author Kengo TODA
 */
public final class VisitedMethodFinder extends EmptyVisitor {
    private final String targetMethodName;
    private final String targetMethodDescriptor;

    private Multimap<JavaClass, MethodDescriptor> visitedMethods = HashMultimap.create();

    /**
     * Type of reference in the top of operand stack.
     */
    private Type lastPushedType;

    public VisitedMethodFinder(@Nonnull String targetMethodName, @Nonnull String targetMethodDescriptor) {
        this.targetMethodName = checkNotNull(targetMethodName);
        this.targetMethodDescriptor = checkNotNull(targetMethodDescriptor);
    }

    @Nonnull
    private Multimap<JavaClass, MethodDescriptor> getVisitedMethods() {
        return visitedMethods;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals(targetMethodName) && descriptor.equals(targetMethodDescriptor)) {
            return this;
        } else {
            // We do not have to analyze this method.
            // Returning null let ASM skip parsing this method.
            return null;
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);
        lastPushedType = Type.getType(desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        super.visitMethodInsn(opcode, owner, name, desc);

        try {
            JavaClass calledClass = Repository.lookupClass(owner);
            Method method = findFrom(calledClass, name, desc).get();

            if (calledClass.getClassName().equals("java.lang.StringBuilder") && name.equals("append") && desc.equals("(Ljava/lang/Object;)Ljava/lang/StringBuilder;")) {
                // consider that stringBuilder.append(obj) as stringBuilder.append(obj.toString())

                JavaClass appendedClass = Repository.lookupClass(lastPushedType.getInternalName());
                Optional<Method> implecitlyCalledToString = findFrom(appendedClass, "toString", "()Ljava/lang/String;");
                if (implecitlyCalledToString.isPresent()) {
                    visitedMethods.put(appendedClass, BCELUtil.getMethodDescriptor(calledClass, implecitlyCalledToString.get()));
                } else {
                    // FIXME support Collection<T> or other class which has no toString method
                    throw new UnsupportedOperationException("this findbugs plugin has not support collection yet");
                }
            }

            visitedMethods.put(calledClass, BCELUtil.getMethodDescriptor(calledClass, method));
            lastPushedType = Type.getReturnType(desc);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @ParametersAreNonnullByDefault
    // TODO how to handle default interface in Java 8?
    private Optional<Method> findFrom(JavaClass clazz, String methodName, String desc) throws ClassNotFoundException {
        do {
            for (Method method : clazz.getMethods()) {
                if (methodName.equals(method.getName()) && desc.equals(method.getSignature())) {
                    return Optional.of(method);
                }
            }
        } while (!clazz.isInterface() && (clazz = clazz.getSuperClass()) != null);

        return Optional.absent();
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public static Multimap<JavaClass, MethodDescriptor> listVisitedMethodFrom(JavaClass javaClass, MethodDescriptor methodDescriptor) {
        // TODO optimization: memorize it to reduce bytecode parsing
        byte[] classByteCode = javaClass.getBytes();
        ClassReader reader = new ClassReader(classByteCode);
        VisitedMethodFinder visitedMethodFinder = new VisitedMethodFinder(methodDescriptor.getName(), methodDescriptor.getSignature());

        reader.accept(visitedMethodFinder, 0);
        return visitedMethodFinder.getVisitedMethods();
    }
}
