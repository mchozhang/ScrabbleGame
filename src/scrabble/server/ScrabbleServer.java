package scrabble.server;

import java.io.*;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * This server class instantiates a Scrabble Manager object
 * and registers it with the naming service.
 */
public class ScrabbleServer {
    static Registry registry;

    public static void main(String[] args) {
        String policyPath = getPolicyPath();
        System.setProperty("java.security.policy", "file:" + policyPath);
        System.setSecurityManager(new SecurityManager());
        try {
            ScrabbleManager scrabbleManager = new ScrabbleManagerImpl();

            // register remote object, wouldn't throw exception even name is registered.
            // start server with url rmi://localhost:1099/scrabble
            registry = LocateRegistry.createRegistry(1099);
            registry.rebind("scrabble", scrabbleManager);

            System.out.println("Waiting for invocations from clients");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * in jar, copy the default policy to the working directory
     *
     * @return default policy file path
     */
    public static String getPolicyPath() {
        URL url = ScrabbleManager.class.getClassLoader().getResource("security.policy");
        if (url.toString().startsWith("jar:")) {
            try {
                File jarFile = getJarFilePath();
                String path = jarFile.getParent();

                File file = new File(path + "/security.policy");
                if (file.exists()) {
                    return file.getAbsolutePath();
                } else {
                    InputStream inputStream = ScrabbleManager.class.getClassLoader().getResourceAsStream("security.policy");
                    OutputStream outputStream = new FileOutputStream(file);
                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    inputStream.close();
                    outputStream.close();
                    return file.getAbsolutePath();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return url.getPath();
        }
    }

    /**
     * get jar file path
     * @return path
     */
    private static File getJarFilePath() {
        try {
            return new File(ScrabbleManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
