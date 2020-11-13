package lab2;

import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;

public class GuessGameDump implements Opcodes {

    public static void main(String[] args) throws Exception{
        File file = new File("G:\\Programms\\JavaSoftOptimization\\src\\main\\java\\lab2\\GuessGame.class");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(dump());
    }

    private static byte[] dump(){

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_PUBLIC + ACC_SUPER, "GuessGame", null, "java/lang/Object", null);
        classWriter.visitSource("GuessGame2.java", null);

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitEnd();
        }

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

            Label label0 = new Label();
            methodVisitor.visitTypeInsn(NEW, "java/util/Random");
            methodVisitor.visitInsn(DUP);

            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/time/Instant", "now", "()Ljava/time/Instant;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/time/Instant", "hashCode", "()I", false);
            methodVisitor.visitInsn(I2L);

            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Random", "<init>", "(J)V", false);
            methodVisitor.visitVarInsn(ASTORE, 1);
            methodVisitor.visitVarInsn(ALOAD, 1);

            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Random", "nextInt", "()I", false);

            methodVisitor.visitIntInsn(BIPUSH, 100);
            methodVisitor.visitInsn(IREM);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitVarInsn(ISTORE, 2);

            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("I guess number");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            methodVisitor.visitTypeInsn(NEW, "java/util/Scanner");
            methodVisitor.visitInsn(DUP);

            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
            methodVisitor.visitVarInsn(ASTORE, 3);

            Label label1  = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
            methodVisitor.visitVarInsn(ISTORE, 4);

            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 2);

            Label label2 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPNE, label2);

            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Your guess is correct. Congratulations!");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            Label label3 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label3);

            methodVisitor.visitLabel(label2);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 2);

            Label label4 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGE, label4);

            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Your guess is smaller than the secret number.");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            methodVisitor.visitJumpInsn(GOTO, label3);

            methodVisitor.visitLabel(label4);
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn("Your guess is greater than the secret number.");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            methodVisitor.visitLabel(label3);
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitJumpInsn(IF_ICMPNE, label1);
            methodVisitor.visitInsn(RETURN);

            methodVisitor.visitLocalVariable("args", "[Ljava/lang/String;", null, label0, label4, 0);
            methodVisitor.visitLocalVariable("random", "Ljava/util/Random;", null, label0, label4, 1);
            methodVisitor.visitLocalVariable("i", "I", null, label0, label4, 2);
            methodVisitor.visitLocalVariable("keyboard", "Ljava/util/Scanner;", null, label0, label4, 3);
            methodVisitor.visitLocalVariable("guess", "I", null, label1, label4, 4);
            methodVisitor.visitMaxs(4, 5);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}




