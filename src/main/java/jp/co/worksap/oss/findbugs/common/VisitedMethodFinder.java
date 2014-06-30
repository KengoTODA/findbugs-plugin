package jp.co.worksap.oss.findbugs.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.internalAnnotations.SlashedClassName;

/**
 * <p>Simple ClassVisitor implementation to find visited methods in the specified method.</p>
 * <p>To create instance, you need to provide name and descriptor to specify the target method.</p>
 *
 * @author Kengo TODA
 */
public final class VisitedMethodFinder extends EmptyVisitor {
    private final String targetMethodName;
    private final String targetMethodDescriptor;
    private final Set<MethodDescriptor> visitedMethods = Sets.newHashSet();

    /**
     * Type of reference in the top of operand stack.
     */
    private Type lastPushedType;

    public VisitedMethodFinder(@Nonnull String targetMethodName, @Nonnull String targetMethodDescriptor) {
        this.targetMethodName = checkNotNull(targetMethodName);
        this.targetMethodDescriptor = checkNotNull(targetMethodDescriptor);
    }

    @Nonnull
    private Set<MethodDescriptor> getVisitedMethods() {
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
            if (owner.equals("java/lang/StringBuilder") && name.equals("append") && desc.equals("(Ljava/lang/Object;)Ljava/lang/StringBuilder;")) {
                // consider that stringBuilder.append(obj) as stringBuilder.append(obj.toString())

                Optional<MethodDescriptor> implecitlyCalledToString = findFrom(lastPushedType.getInternalName(), "toString", "()Ljava/lang/String;", false);
                if (implecitlyCalledToString.isPresent()) {
                    visitedMethods.add(implecitlyCalledToString.get());
                } else {
                    // FIXME support Collection<T> or other class which has no toString method
                    throw new UnsupportedOperationException("this findbugs plugin has not support collection yet:" + lastPushedType.getInternalName());
                }
            }

            visitedMethods.add(new MethodDescriptor(owner, name, desc, opcode == Opcodes.INVOKESTATIC));
            lastPushedType = Type.getReturnType(desc);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @ParametersAreNonnullByDefault
    // TODO how to handle default interface in Java 8?
    private Optional<MethodDescriptor> findFrom(@SlashedClassName final String className, final String methodName, final String methodDesc, final boolean isStatic) throws IOException {
        if (className == null) {
            return Optional.absent();
        }

        final AtomicReference<MethodDescriptor> foundMethod = new AtomicReference<MethodDescriptor>();
        final AtomicReference<String> foundSuperClass = new AtomicReference<String>();
        ClassReader reader = new ClassReader(className);
        EmptyVisitor visitor = new EmptyVisitor() {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                if ((Opcodes.ACC_INTERFACE & access) != 0) {
                    // this is interface, no need to parse super class
                    // TODO add test case for annotation, enum and more
                } else {
                    foundSuperClass.set(superName);
                }
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals(methodName) && desc.equals(methodDesc)) {
                    foundMethod.set(new MethodDescriptor(className, name, desc, isStatic));
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };

        reader.accept(visitor, 0);
        return Optional.fromNullable(foundMethod.get()).or(findFrom(foundSuperClass.get(), methodName, methodDesc, isStatic));
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    public static Set<MethodDescriptor> listVisitedMethodFrom(@DottedClassName String className, MethodDescriptor methodDescriptor) {
        // TODO optimization: memorize it to reduce bytecode parsing
        try {
            ClassReader reader = new ClassReader(className);
            VisitedMethodFinder visitedMethodFinder = new VisitedMethodFinder(methodDescriptor.getName(), methodDescriptor.getSignature());

            reader.accept(visitedMethodFinder, 0);
            return visitedMethodFinder.getVisitedMethods();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
