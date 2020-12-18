import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("Hello! I`m java agent");
        instrumentation.addTransformer(new ClassTransformer());
        System.out.println("count load: " + instrumentation.getAllLoadedClasses().length);
    }
}
