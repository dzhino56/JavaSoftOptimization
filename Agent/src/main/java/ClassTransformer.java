import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ClassTransformer implements ClassFileTransformer {
    private final ClassPool classPool;
    private boolean isAdded = false;

    public ClassTransformer() {
        classPool = ClassPool.getDefault();
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        try {
            CtClass ctClass = classPool.get("TransactionProcessor");

            if (!isAdded) {
                CtField minimalTime = new CtField(CtClass.doubleType, "minimalTime", ctClass);
                CtField maximalTime = new CtField(CtClass.doubleType, "maximalTime", ctClass);
                CtField averageTime = new CtField(CtClass.doubleType, "averageTime", ctClass);
                CtField ctField = new CtField(CtClass.intType, "ctField", ctClass);

                minimalTime.setModifiers(Modifier.STATIC);
                maximalTime.setModifiers(Modifier.STATIC);
                averageTime.setModifiers(Modifier.STATIC);
                ctField.setModifiers(Modifier.STATIC);

                ctClass.addField(minimalTime);
                ctClass.addField(maximalTime);
                ctClass.addField(averageTime);
                ctClass.addField(ctField);
                isAdded = true;

                CtMethod main = ctClass.getDeclaredMethod("main");
                main.insertBefore("minimalTime =  Double.MAX_VALUE;");
                main.insertBefore("maximalTime =  Double.MIN_VALUE;");
                main.insertBefore("averageTime =  0.0;");
                main.insertBefore("ctField =  0;");

                CtMethod processTransaction = ctClass.getDeclaredMethod("processTransaction");
                processTransaction.addLocalVariable("startTime", CtClass.longType);
                processTransaction.insertBefore("startTime = System.currentTimeMillis();");
                processTransaction.insertAfter("ctField = ctField + 1;");

                StringBuilder endBlock = new StringBuilder();

                processTransaction.addLocalVariable("endTime", CtClass.longType);
                processTransaction.addLocalVariable("opTime", CtClass.floatType);
                endBlock.append("endTime = System.currentTimeMillis();");
                endBlock.append("opTime = (float)(endTime-startTime)/1000.0;");
                endBlock.append("if (opTime < minimalTime) {minimalTime = opTime;}");
                endBlock.append("if (opTime > maximalTime) {maximalTime = opTime;}");
                endBlock.append("averageTime += opTime;");

                processTransaction.insertAfter(endBlock.toString());
                main.insertAfter("{ System.out.println( \"mintime:\" + minimalTime); }");
                main.insertAfter("{ System.out.println( \"maxtime:\" + maximalTime); }");
                main.insertAfter("{ System.out.println( \"avtime:\" + averageTime/ctField); }");
            }
            ctClass.instrument(
                    new ExprEditor() {
                        public void edit(MethodCall m)
                                throws CannotCompileException {
                            if (m.getMethodName().equals("processTransaction")) {
                                m.replace("{ $1 = $1 + 99; $_ = $proceed($$); }");
                            }
                        }
                    });

            ctClass.writeFile();

        } catch(NotFoundException | CannotCompileException | IOException e){
            System.out.println("class: " + className);
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}