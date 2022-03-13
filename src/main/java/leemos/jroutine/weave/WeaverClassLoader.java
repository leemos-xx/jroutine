package leemos.jroutine.weave;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

public class WeaverClassLoader extends URLClassLoader {

    private static final int BUFFER_SIZE = 4096;

    private final AccessControlContext access;
    private ClassTransformer transformer;

    public WeaverClassLoader(URL[] urls, ClassTransformer transformer) {
        super(urls);
        this.transformer = transformer;
        access = AccessController.getContext();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> theClass = findLoadedClass(name);
        if (theClass != null) {
            return theClass;
        }

        try {
            theClass = getParent().loadClass(name);
            
            if (isRunnable(theClass.getInterfaces())) {
                theClass = findClass(name);
            }
        } catch (Exception e) {
            
        }

        resolveClass(theClass);

        return theClass;
    }

    private boolean isRunnable(Class<?>[] interfaces) {
        for (Class<?> inter: interfaces) {
            if (inter == Runnable.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String classFileName = name.replace('.', '/') + ".class";

        InputStream stream = getResourceAsStream(classFileName);
        if (stream == null) {
            throw new ClassNotFoundException(name);
        }

        try {
            return getClassFromStream(stream, name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private Class<?> getClassFromStream(InputStream stream, String classname) throws IOException, SecurityException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];

            while ((bytesRead = stream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] classData = baos.toByteArray();
            return defineClassFromData(classData, classname);

        } finally {
            baos.close();
        }
    }

    private Class<?> defineClassFromData(final byte[] classData, final String classname) {
        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            public Class<?> run() {
                int i = classname.lastIndexOf('.');
                if (i > 0) {
                    final String packageName = classname.substring(0, i);
                    final Package pkg = getPackage(packageName);
                    if (pkg == null) {
                        definePackage(packageName, null, null, null, null, null, null, null);
                    }
                }

                final byte[] newData = transformer.transform(classData);
                final ProtectionDomain domain = this.getClass().getProtectionDomain();
                return defineClass(classname, newData, 0, newData.length, domain);
            }
        }, access);
    }

}
