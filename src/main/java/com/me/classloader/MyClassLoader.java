package com.me.classloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import lombok.Setter;

@Setter
public class MyClassLoader extends URLClassLoader {

    private ClassLoader parent;
    private ClassLoader system;

    /**
     * The cache of ResourceEntry for classes and resources we have loaded, keyed by
     * resource name.
     */
    protected HashMap resourceEntries = new HashMap();

    /**
     * The list of not found resources.
     */
    protected HashMap notFoundResources = new HashMap();

    /**
     * Has external repositories.
     */
    protected boolean hasExternalRepositories = false;

    /**
     * The list of JARs, in the order they should be searched for locally loaded
     * classes or resources.
     */
    protected JarFile[] jarFiles = new JarFile[0];

    /**
     * The list of local repositories, in the order they should be searched for
     * locally loaded classes or resources.
     */
    protected String[] repositories = new String[0];

    /**
     * Associated directory context giving access to the resources in this webapp.
     */
    protected DirContext resources = null;

    /**
     * Use anti JAR locking code, which does URL rerouting when accessing resources.
     */
    boolean antiJARLocking = false;

    /**
     * Path where resources loaded from JARs will be extracted.
     */
    protected File loaderDir = null;

    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected File[] jarRealFiles = new File[0];
    /**
     * The list of resources which should be checked when checking for
     * modifications.
     */
    protected String[] paths = new String[0];

    /** 
     * Repositories translated as path in the work directory (for Jasper
     * originally), but which is used to generate fake URLs should getURLs be
     * called.
     */
    protected File[] files = new File[0];

    /**
     * The path which will be monitored for added Jar files.
     */
    protected String jarPath = null;

    /**
     * The list of JARs, in the order they should be searched for locally loaded
     * classes or resources.
     */
    protected String[] jarNames = new String[0];

    public MyClassLoader(URL[] urls) {
        super(urls);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {

        Class clazz = null;

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.2) Try loading the class with the system class loader, to prevent
        // the webapp from overriding J2SE classes
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        boolean delegateLoad = false;

        // (1) Delegate to our parent if requested
        if (delegateLoad) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        // (2) Search local repositories
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }

        // (3) Delegate to parent unconditionally
        if (!delegateLoad) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        // This class was not found
        throw new ClassNotFoundException(name);
    }

    public Class findClass(String name) throws ClassNotFoundException {

        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class clazz = null;
        try {
            try {
                clazz = findClassInternal(name);
            } catch (ClassNotFoundException cnfe) {
                if (!hasExternalRepositories) {
                    throw cnfe;
                }
            } catch (AccessControlException ace) {
                ace.printStackTrace();
                throw new ClassNotFoundException(name);
            } catch (RuntimeException e) {
                throw e;
            }
            if ((clazz == null) && hasExternalRepositories) {
                try {
                    clazz = super.findClass(name);
                } catch (AccessControlException ace) {
                    throw new ClassNotFoundException(name);
                } catch (RuntimeException e) {
                    throw e;
                }
            }
            if (clazz == null) {
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            throw e;
        }

        // Return the class we have located
        return (clazz);

    }

    // name = package+className
    protected Class findClassInternal(String name) throws ClassNotFoundException {

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + ".class";

        ResourceEntry entry = null;

        entry = findResourceInternal(name, classPath);

        if ((entry == null) || (entry.binaryContent == null))
            throw new ClassNotFoundException(name);

        Class clazz = entry.loadedClass;
        if (clazz != null)
            return clazz;

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);

        Package pkg = null;

        if (packageName != null) {

            pkg = getPackage(packageName);

            // Define the package (if null)
            if (pkg == null) {
                if (entry.manifest == null) {
                    definePackage(packageName, null, null, null, null, null, null, null);
                } else {
                    definePackage(packageName, entry.manifest, entry.codeBase);
                }
            }

        }

        // Create the code source object
        CodeSource codeSource = new CodeSource(entry.codeBase, entry.certificates);

        if (entry.loadedClass == null) {
            synchronized (this) {
                if (entry.loadedClass == null) {
                    clazz = defineClass(name, entry.binaryContent, 0, entry.binaryContent.length, codeSource);
                    entry.loadedClass = clazz;
                } else {
                    clazz = entry.loadedClass;
                }
            }
        } else {
            clazz = entry.loadedClass;
        }

        return clazz;
    }

    protected ResourceEntry findResourceInternal(String name, String path) {

        if ((name == null) || (path == null))
            return null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null)
            return entry;

        int contentLength = -1;
        InputStream binaryStream = null;

        int jarFilesLength = jarFiles.length;
        int repositoriesLength = repositories.length;

        int i;

        Resource resource = null;

        // 先从文件夹下找class
        for (i = 0; (entry == null) && (i < repositoriesLength); i++) {
            try {

                String fullPath = repositories[i] + path;

                Object lookupResult = resources.lookup(fullPath);
                if (lookupResult instanceof Resource) {
                    resource = (Resource) lookupResult;
                }

                // Note : Not getting an exception here means the resource was
                // found

                entry = new ResourceEntry();
                try {
                    entry.source = getURL(new File(files[i], path));
                    entry.codeBase = entry.source;
                } catch (MalformedURLException e) {
                    return null;
                }
                ResourceAttributes attributes = (ResourceAttributes) resources.getAttributes(fullPath);
                contentLength = (int) attributes.getContentLength();
                entry.lastModified = attributes.getLastModified();

                if (resource != null) {

                    try {
                        binaryStream = resource.streamContent();
                    } catch (IOException e) {
                        return null;
                    }

                    // Register the full path for modification checking
                    // Note: Only syncing on a 'constant' object is needed
                    int j;
                    String[] result = new String[paths.length + 1];
                    for (j = 0; j < paths.length; j++) {
                        result[j] = paths[j];
                    }
                    result[paths.length] = fullPath;
                    paths = result;

                }

            } catch (NamingException e) {
            }
        }

        if ((entry == null) && (notFoundResources.containsKey(name)))
            return null;

        JarEntry jarEntry = null;

        // 从war下找class
        for (i = 0; (entry == null) && (i < jarFilesLength); i++) {

            jarEntry = jarFiles[i].getJarEntry(path);

            if (jarEntry != null) {

                entry = new ResourceEntry();
                try {
                    entry.codeBase = getURL(jarRealFiles[i]);
                    String jarFakeUrl = entry.codeBase.toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + path;
                    entry.source = new URL(jarFakeUrl);
                } catch (MalformedURLException e) {
                    return null;
                }
                contentLength = (int) jarEntry.getSize();
                try {
                    entry.manifest = jarFiles[i].getManifest();
                    binaryStream = jarFiles[i].getInputStream(jarEntry);
                } catch (IOException e) {
                    return null;
                }

                // Extract resources contained in JAR to the workdir
                if (antiJARLocking && !(path.endsWith(".class"))) {
                    byte[] buf = new byte[1024];
                    File resourceFile = new File(loaderDir, jarEntry.getName());
                    if (!resourceFile.exists()) {
                        Enumeration entries = jarFiles[i].entries();
                        while (entries.hasMoreElements()) {
                            JarEntry jarEntry2 = (JarEntry) entries.nextElement();
                            if (!(jarEntry2.isDirectory()) && (!jarEntry2.getName().endsWith(".class"))) {
                                resourceFile = new File(loaderDir, jarEntry2.getName());
                                resourceFile.getParentFile().mkdirs();
                                FileOutputStream os = null;
                                InputStream is = null;
                                try {
                                    is = jarFiles[i].getInputStream(jarEntry2);
                                    os = new FileOutputStream(resourceFile);
                                    while (true) {
                                        int n = is.read(buf);
                                        if (n <= 0) {
                                            break;
                                        }
                                        os.write(buf, 0, n);
                                    }
                                } catch (IOException e) {
                                    // Ignore
                                } finally {
                                    try {
                                        if (is != null) {
                                            is.close();
                                        }
                                    } catch (IOException e) {
                                    }
                                    try {
                                        if (os != null) {
                                            os.close();
                                        }
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        if (entry == null) {
            synchronized (notFoundResources) {
                notFoundResources.put(name, name);
            }
            return null;
        }

        // 读取class字节流
        if (binaryStream != null) {
            byte[] binaryContent = new byte[contentLength];
            try {
                int pos = 0;
                while (true) {
                    int n = binaryStream.read(binaryContent, pos, binaryContent.length - pos);
                    if (n <= 0)
                        break;
                    pos += n;
                }
                binaryStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            entry.binaryContent = binaryContent;

            // The certificates are only available after the JarEntry
            // associated input stream has been fully read
            if (jarEntry != null) {
                entry.certificates = jarEntry.getCertificates();
            }

        }

        // Add the entry in the local resource repository
        synchronized (resourceEntries) {
            // Ensures that all the threads which may be in a race to load
            // a particular class all end up with the same ResourceEntry
            // instance
            ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
            if (entry2 == null) {
                resourceEntries.put(name, entry);
            } else {
                entry = entry2;
            }
        }

        return entry;
    }

    protected URL getURL(File file) throws MalformedURLException {

        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }

        // return new URL("file:" + realFile.getPath());
        URLEncoder urlEncoder = new URLEncoder();
        urlEncoder.addSafeCharacter(',');
        urlEncoder.addSafeCharacter(':');
        urlEncoder.addSafeCharacter('-');
        urlEncoder.addSafeCharacter('_');
        urlEncoder.addSafeCharacter('.');
        urlEncoder.addSafeCharacter('*');
        urlEncoder.addSafeCharacter('/');
        urlEncoder.addSafeCharacter('!');
        urlEncoder.addSafeCharacter('~');
        urlEncoder.addSafeCharacter('\'');
        urlEncoder.addSafeCharacter('(');
        urlEncoder.addSafeCharacter(')');

        return new URL(urlEncoder.encode(realFile.toURL().toString()));
    }

    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     * 
     * @param repository
     * @param file
     */
    protected void addRepository(String repository, File file) {

        // Note : There should be only one (of course), but I think we should
        // keep this a bit generic
        if (repository == null)
            return;

        int i;

        // Add this repository to our internal list
        String[] result = new String[repositories.length + 1];
        for (i = 0; i < repositories.length; i++) {
            result[i] = repositories[i];
        }
        result[repositories.length] = repository;
        repositories = result;

        // Add the file to the list
        File[] result2 = new File[files.length + 1];
        for (i = 0; i < files.length; i++) {
            result2[i] = files[i];
        }
        result2[files.length] = file;
        files = result2;
    }

    protected void addJar(String jar, JarFile jarFile, File file) throws IOException {

        if (jar == null)
            return;
        if (jarFile == null)
            return;
        if (file == null)
            return;

        int i;

        if ((jarPath != null) && (jar.startsWith(jarPath))) {

            String jarName = jar.substring(jarPath.length());
            while (jarName.startsWith("/")||jarName.startsWith("\\"))
                jarName = jarName.substring(1);

            String[] result = new String[jarNames.length + 1];
            for (i = 0; i < jarNames.length; i++) {
                result[i] = jarNames[i];
            }
            result[jarNames.length] = jarName;
            jarNames = result;

        }

//        // Register the JAR for tracking
//        String[] result = new String[paths.length + 1];
//        for (i = 0; i < paths.length; i++) {
//            result[i] = paths[i];
//        }
//        result[paths.length] = jar;
//        paths = result;

        JarFile[] result2 = new JarFile[jarFiles.length + 1];
        for (i = 0; i < jarFiles.length; i++) {
            result2[i] = jarFiles[i];
        }
        result2[jarFiles.length] = jarFile;
        jarFiles = result2;

        // Add the file to the list
        File[] result4 = new File[jarRealFiles.length + 1];
        for (i = 0; i < jarRealFiles.length; i++) {
            result4[i] = jarRealFiles[i];
        }
        result4[jarRealFiles.length] = file;
        jarRealFiles = result4;

        // Load manifest
        Manifest manifest = jarFile.getManifest();
    }
}
