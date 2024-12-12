package me.ender;

import haven.FromResource;
import haven.Resource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResSourceUpdater {
    public static void main(String[] args) throws Exception {
	System.out.println("START UPDATING RES SOURCES");
	Set<String> resources = collectAnnotations();
	for (String resource : resources) {
	    System.out.printf("UPDATING '%s'%n", resource);
	    Resource.cmd_getcode(new String[]{
		"-U", "https://game.havenandhearth.com/res/",
		"-o", "../resources/loftar-res-sources",
		resource
	    });
	}
	System.out.println("DONE UPDATING RES SOURCES");
    }
    
    private static Set<String> collectAnnotations() {
	System.out.println("COLLECT ANNOTATIONS");
	Set<String> res = new HashSet<>();
	List<String> classes = enumerateClasses();
	for (String aClass : classes) {
	    try {
		Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(aClass);
		FromResource from = clazz.getAnnotation(FromResource.class);
		if(from != null) {
		    System.out.printf("res: '%s', class: '%s'%n", from.name(), clazz.getName());
		    res.add(from.name());
		}
	    } catch (Exception | NoClassDefFoundError e) {
		//throw new RuntimeException(e);
	    }
	}
	return res;
    }
    
    private static List<String> enumerateClasses() {
	System.out.println("ENUMERATE CLASSES");
	List<String> classNames = new ArrayList<>();
	try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(getJAR()))) {
	    for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
		if(!entry.isDirectory() && entry.getName().endsWith(".class")) {
		    // This ZipEntry represents a class. Now, what class does it represent?
		    String className = entry.getName().replace('/', '.'); // including ".class"
		    classNames.add(className.substring(0, className.length() - ".class".length()));
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	return classNames;
    }
    
    private static Path getJAR() {
	try {
	    return Paths.get(ResSourceUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
	} catch (URISyntaxException e) {
	    throw new RuntimeException(e);
	}
    }
}
