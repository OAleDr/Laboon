package br.com.laboon.core.command;

import br.com.laboon.core.command.CommandClass;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class CommandScanner {

    private CommandScanner() {
    }

    public static List<Class<? extends CommandClass>> scan(String packageName) {

        List<Class<? extends CommandClass>> commands = new ArrayList<>();

        String path = packageName.replace('.', '/');

        try {

            ClassLoader classLoader = CommandScanner.class.getClassLoader();

            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {

                URL resource = resources.nextElement();

                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {

                    scanDirectory(packageName, new File(URI.create(resource.toString())), commands);

                } else if ("jar".equals(protocol)) {

                    scanJar(packageName, resource, commands);
                }
            }

        } catch (IOException exception) {

            throw new IllegalStateException("Não foi possível procurar comandos no pacote: " + packageName, exception);
        }

        return commands;
    }

    private static void scanDirectory(String packageName, File directory, List<Class<? extends CommandClass>> commands) {

        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                scanDirectory(packageName + "." + file.getName(), file, commands);

                continue;
            }

            String fileName = file.getName();

            if (!fileName.endsWith(".class")) {
                continue;
            }

            String className = packageName + "." + fileName.substring(0, fileName.length() - 6);

            addCommand(className, commands);
        }
    }

    private static void scanJar(String packageName, URL resource, List<Class<? extends CommandClass>> commands) {

        try {

            JarURLConnection connection = (JarURLConnection) resource.openConnection();

            try (JarFile jar = connection.getJarFile()) {

                String packagePath = packageName.replace('.', '/') + "/";

                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {

                    JarEntry entry = entries.nextElement();

                    String name = entry.getName();

                    if (entry.isDirectory() || !name.endsWith(".class") || !name.startsWith(packagePath)) {

                        continue;
                    }

                    String className = name.substring(0, name.length() - 6).replace('/', '.');

                    addCommand(className, commands);
                }
            }

        } catch (IOException exception) {

            throw new IllegalStateException("Não foi possível procurar comandos no JAR.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addCommand(String className, List<Class<? extends CommandClass>> commands) {

        try {

            Class<?> type = Class.forName(className);

            if (!CommandClass.class.isAssignableFrom(type) || type.isInterface() || type.isAnnotation() || type.isEnum()) {

                return;
            }

            commands.add((Class<? extends CommandClass>) type);

        } catch (ClassNotFoundException exception) {

            throw new IllegalStateException("Não foi possível carregar a classe: " + className, exception);

        } catch (LinkageError ignored) {
            // A classe não pôde ser carregada neste ambiente.
        }
    }
}