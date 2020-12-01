package lab3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

class ModuleLoader extends ClassLoader {
    private static final String packageName = "ru.nsu.ojp2020.";
    private String pathToBin;

    ModuleLoader(String pathToBin, ClassLoader parent) {
        super(parent);
        this.pathToBin = pathToBin;
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            byte[] b = fetchClassFromFS(pathToBin + className + ".class");
            return defineClass(packageName + className, b, 0, b.length);
        } catch (IOException ex) {
            return super.findClass(className);
        }
    }

    private byte[] fetchClassFromFS(String path) throws IOException {
        InputStream is = new FileInputStream(new File(path));

        // Get the size of the file
        long length = new File(path).length();

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + path);
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}

public class Main {

    public static void main(String[] args) {
        String modulePath = args[0];

        ModuleLoader loader = new ModuleLoader(modulePath, ClassLoader.getSystemClassLoader());

        File dir = new File(modulePath);
        String[] modules = dir.list();

        boolean flag = false;
        assert modules != null;
        for (String module : modules) {
            try {
                String moduleName = module.split(".class")[0];
                Class clazz = loader.loadClass(moduleName);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals("getSecurityMessage")) {
                        Method method1 = clazz.getDeclaredMethod("getSecurityMessage");
                        method1.setAccessible(true);
                        System.out.println(clazz.getName() + " " + method1.invoke(clazz.newInstance()));
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    System.out.println(clazz.getName() + " No such Method");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
