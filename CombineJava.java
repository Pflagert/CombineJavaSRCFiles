import java.io.*;
import java.util.*;

public class CombineJava{
	static int count=0;
	static Check c=new Check();
	static FileOutputStream fos;
	static int progress = 0;
	static int maxPrint = 15;
	// list of files that have been copied
	static ArrayList<File> written=new ArrayList<File>();
	// list of unique imports
	static ArrayList<String> imports = new ArrayList<String>();
	// list of unique packages
	static ArrayList<String> packages = new ArrayList<String>();

/*
    	static String mainFileName = "src/typecheck/Typecheck.java";
    	static String sourceDirectoryName = "/home/pflagert/git/CS453/HW2/src";
	static String destFilePath = "/home/pflagert/git/CS453/HW2/combinedSrc/";
	static String destFileName = "Typecheck.java";
	static String tmpFileName = null;
*/
	// name of the file with public class *
	static String mainFileName = null;
	// directory where either subdirectories or source files are located
	static String sourceDirectoryName = null;
	static String destFilePath = null;
	static String destFileName = null;
	static String tmpFileName = null;
	static File mainFile; // a file with public class *
	static File sourceDirectory;
	static File destFileDirectory; // the directory where destFile and tmpFile will be stored
	static File destFile; // the combined file
	static File tmpFile; // temporary file

	public static void main(String ar[])throws Exception{
		requestFileNames();
		checkFileErrors();
		long startTime = System.currentTimeMillis();
		long endTime;
		int seconds;

		fos=new FileOutputStream(tmpFile.getPath());
		writeMainFile(mainFile);
		combine(sourceDirectoryName);
		fos.close();

		printPackages();
		fixImports();
		printImports();

		fos = new FileOutputStream(destFile.getPath());
		createDestFile(tmpFile);		
		fos.close();

		endTime = System.currentTimeMillis();
		int millis = (int) (endTime - startTime);
		seconds = (int) (((millis) / 1000) % 60);
		if(seconds > 0) {
			System.out.println("\nFinished in " + seconds + " seconds");
		}
		else {
			System.out.println("\nFinished in " + millis + " milliseconds");
		}
	}


	/**
	 * Asks the user for the paths / names of files.
	 * If they are not already set.
	 */
	public static void requestFileNames() {
		Scanner scan = new Scanner(System.in);
		if(mainFileName == null) {
			System.out.println("Enter the path to your main file: ");
			mainFileName = scan.nextLine();
		}
		mainFile=new File(mainFileName);
		checkFileErrors();
		if(sourceDirectoryName == null) {
			System.out.println("Enter the path to your java source files: ");
			sourceDirectoryName = scan.nextLine();
		}
		sourceDirectory=new File(sourceDirectoryName);
		checkFileErrors();
		if(destFilePath == null) {
			System.out.println("Enter the directory you want the output file to be stored in: ");
			destFilePath = scan.nextLine();
			if(!destFilePath.endsWith("/")) {
				destFilePath += "/";
			}
		}
		destFileDirectory = new File(destFilePath);
		checkFileErrors();
		if(destFileName == null) {
			System.out.println("Enter the name of the output file you would like to create: ");
			destFileName = scan.nextLine();
		}
		destFile=new File(destFilePath+destFileName);
		checkFileErrors();
		if(tmpFileName == null) {
			tmpFileName = "NO_IMPORTS_" + destFileName;
		}
		tmpFile=new File(destFilePath+tmpFileName);
		scan.close();
	}


	/**
	 * Checks for conditions that need to be / should be true
	 * for this program to function correctly
	 */
	public static void checkFileErrors() {
		// check if the main file exists, if not exit
		if(mainFile != null && !mainFile.exists()) {
			System.out.println("Main file: " + mainFile.getPath() 
			+ " does not exist");
			System.exit(1);
		}
		if(sourceDirectory != null) {
			// check if source directory exists and ensure that it is a directory 
			if(sourceDirectory.exists() && !sourceDirectory.isDirectory()) {
				System.out.println("Destination directory: " + sourceDirectory.getPath() 
				+ " is not a directory");
				System.exit(1);
			}
			else if(!sourceDirectory.exists()) {
				System.out.println("Source Directory: " + sourceDirectory.getPath() +
						" does not exists."); 
				System.exit(1);
			}
		}
		// check if the directory exists and ensure that it is a directory
		if(destFileDirectory != null) {
			if(destFileDirectory.exists() && !destFileDirectory.isDirectory()) {
				System.out.println("Destination directory: " + destFileDirectory.getPath() 
				+ " is not a directory");
				System.exit(1);
			}
			else if(!destFileDirectory.exists()) {
				System.out.println("Directory: " + destFileDirectory.getPath() +
						" does not exists.\n" + 
						"Creating: " +  destFileDirectory.getPath());
				if(!destFileDirectory.mkdirs()){
					System.out.println("Failded to create: " + destFileDirectory.getPath());
					System.exit(1);
				}
			}
		}
		// check if the destination file exists, if so exit

		if(destFile != null && destFile.exists()) {
			System.out.println("Destination file: " + destFile.getPath() 
			+ " already exists");
			System.exit(1);
		}
		// check if the temporary file exists, if so exit
		if(tmpFile != null && tmpFile.exists()) {
			System.out.println("Temporary file: " + tmpFile.getPath() 
			+ " already exists");
			System.exit(1);
		}
	}

	public static void print(String s) {
		if(progress < maxPrint) {
			progress++;
			System.out.print(s);
		}
	}

	public static void printRest(String s) {
		for(;progress<maxPrint;progress++)
			System.out.print(".");
		progress = 0;
		System.out.println(s);
	}

	/**
	 * Removes packages from the list of imports found
	 */
	public static void fixImports() {
		System.out.print("\nRemoving package imports");
		Iterator<String> it = imports.iterator();
		String imp;
		boolean remove;
		while (it.hasNext()) {
			remove = false;
			imp = it.next();
			print(".");
			for(String pack: packages) {
				print(".");
				if(imp.contains(pack)) {
					remove = true;
					break;
				}
			}
			if (remove) {
				it.remove();
			}
		}
		printRest("\t[done]");
	}

	/**
	 * Prints the unique list of imports found
	 */
	public static void printImports() {
		System.out.println("\nFound these imports: ");
		for(String imPort: imports) {
			System.out.println("\t" + imPort);
		}
	}

	/**
	 * Prints the unique list of packages found
	 */
	public static void printPackages() {
		System.out.println("\nFound these packages: ");
		for(String pack: packages) {
			System.out.println("\t" + pack);
		}
	}

	/**
	 * Writes the required imports.
	 * Copies the temporary file to the destination file.
	 * 
	 */
	public static void createDestFile(File tmp) throws IOException {
		System.out.println("\nCreating " + destFile.getPath());
		System.out.print("\tWriting imports");
		long total = 0;
		for(String imp: imports) {
			byte[] bytes = (imp+"\n").getBytes();
			total += bytes.length;
			fos.write(bytes);
			print(".");
		}
		printRest(" [done]");
		System.out.print("\tCopying temporary file");
		InputStreamReader isr1=new InputStreamReader(new FileInputStream(tmp));
		BufferedReader br1=new BufferedReader(isr1);
		String s1;

		int every50Lines = 0;
		while( (s1=br1.readLine()) != null) {
			every50Lines++;
			// check for methods that reference a package
			if(s1.contains("public ")) {
				s1 = removePackageRefs(s1);
			}
			byte[] bytes = (s1+"\n").getBytes();
			total += bytes.length;
			fos.write(bytes);
			every50Lines++;
			if(every50Lines == 50) {
				print(".");
				every50Lines = 0;
			}
		}
		br1.close();
		printRest("\t[done]\n\t" + "Wrote [" + total + "] bytes\n" 
				+ "\nCopied " + count + " files");
	}

	/**
	 * This method searches a directory recursively,
	 * if there exists *.java it 
	 */
	public static void combine( String dir)throws Exception{

		File f=new File(dir);
		String list[]=f.list();

		for(int i=0;i<list.length;i++){
			String filename=list[i];
			File f2=new File(f,filename);
			if(!f2.isFile()){
				System.out.println(f2.getPath());
				combine(f2.getPath());
			}else{
				if(f2.getName().endsWith(".java")&(!f2.getPath().equals(destFile.getPath()))){
					if(f2.getName().equals(mainFile.getName())) {
						System.out.println("\tSkipping " + f2.getPath());
						continue;
					}
					else if(!c.duplecate(written,f2)){
						writeFileAsNestedClass(f2);
					}
					else
						System.out.println("\tDuplicate file found "+f2.getName());
				}
			}
		}    
	}

	/**
	 *  Copy the main file and allow for public class declaration
	 *  add imports and package names to their associated lists
	 */
	static void writeMainFile(File f1) throws IOException {
		InputStreamReader isr1=new InputStreamReader(new FileInputStream(f1));
		BufferedReader br1=new BufferedReader(isr1);
		String s1;
		System.out.print("Copying Main File "+f1.getName());
		int every50Lines = 0;
		int index;
		while( (s1=br1.readLine()) != null) {
			every50Lines++;
			if(s1.contains("package ")) {
				s1 = s1.substring(s1.indexOf("package ")+"package ".length());
				index = 0;
				while( (index = s1.lastIndexOf(';'))!= -1){
					s1 = s1.substring(0,index);					
				}
				if(!packages.contains(s1))
					packages.add(s1);
			}
			else {
				if(s1.contains("import ")) {
					imports.add(s1);
				}
				else {
					fos.write((s1+"\n").getBytes());
				}
			}
			if(every50Lines == 50) {
				print(".");
				every50Lines = 0;
			}
		}
		written.add(f1);
		printRest("\t[done]\n");
		count++;
		br1.close();

	}

	/**
	 * Removes references to packages
	 * for example: 
	 * 	public void doSomething(visitor.visit)
	 * becomes:
	 * 	public void doSomething(visit)
	 */
	public static String removePackageRefs(String s) {
		String _ret = s;
		for(String pack: packages) {
			while(_ret.contains(pack+".")) {
				_ret = _ret.replace(pack+".", "");
			}
		}
		return _ret;
	}

	/**
	 * Copies the file to the temporary file
	 * While doing so, looks for public classes / interfaces and strips the word "public"
	 * also adds imports and package names to their associated lists 
	 */
	public static void writeFileAsNestedClass(File f1) throws IOException{
		InputStreamReader isr1=new InputStreamReader(new FileInputStream(f1));
		BufferedReader br1=new BufferedReader(isr1);
		String s1;
		System.out.print("\tCopying "+f1.getName());
		int every50Lines = 0;
		int index;
		while( (s1=br1.readLine()) != null) {
			// ignore packages
			every50Lines++;
			if(s1.contains("package ")) {
				s1 = s1.substring(s1.indexOf("package ")+"package ".length());
				index = 0;
				while( (index = s1.lastIndexOf(';'))!= -1){
					s1 = s1.substring(0,index);					
				}
				s1 = s1.trim();
				if(!packages.contains(s1))
					packages.add(s1);
			}
			else if(s1.contains("import ")) {
				if(s1.contains("org.junit")) {
					printRest("\t[SKIPPED] JUNIT");;
					br1.close();
					return;
				}
				else if(!imports.contains(s1)) {
					imports.add(s1);
				}
			}
			else {
				if(s1.contains("public class ") || s1.contains("public interface ") ) {
					fos.write( 
							(s1.substring(s1.indexOf("public ")+
									"public ".length())+"\n"
									).getBytes()
							);
				}
				else if(s1.contains("public ")) {
					fos.write(removePackageRefs(s1 + "\n").getBytes());
				}
				else {
					fos.write((s1 + "\n").getBytes());
				}
			}
			if(every50Lines == 50) {
				print(".");
				every50Lines = 0;
			}
		}
		printRest("\t[done]");
		written.add(f1);
		count++;
		CLOSE:
		br1.close();
	}
}

/**
 * Helper class to check for duplicated files
 *
 */
class Check{
	/**
	 * Compares two files to see if they are identical, if they are, one will not be included
	 * in the destination file
	 */
	boolean isSame(File f1,File f2)throws Exception{
		boolean same =true;
		InputStreamReader isr1=new InputStreamReader(new FileInputStream(f1));
		InputStreamReader isr2=new InputStreamReader(new FileInputStream(f2));
		BufferedReader br1=new BufferedReader(isr1);
		BufferedReader br2=new BufferedReader(isr2);
		String s1,s2;
		while((s1=br1.readLine())!=null){
			s2=br2.readLine();
			//System.out.println("\t"+s1+"\n\t"+s2);
			if(!s1.equals(s2)){
				same=false;
				//System.out.println(f1.getPath()+"  is same as  "+f2.getPath());
				break;
			}
		}
		br1.close();
		br2.close();
		return same;
	}

	/**
	 * Compares one file to all other files to see if there is a match, if there is, one will not be included
	 * in the destination file
	 */
	boolean duplecate(ArrayList<File> list,File file)throws Exception{
		boolean exist=false;
		for(File f:list){
			if(f.getName().equals(file.getName()))
			{
				//System.out.println(f.getName()+" matches ");
				if(isSame(f,file))
					return true;
			}
		}
		return exist;
	}
}
