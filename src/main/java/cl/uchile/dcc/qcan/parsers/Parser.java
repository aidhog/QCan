package cl.uchile.dcc.qcan.parsers;

import cl.uchile.dcc.qcan.data.FeatureCounter;
import cl.uchile.dcc.qcan.main.SingleQuery;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.jena.ext.com.google.common.collect.HashMultiset;
import org.apache.jena.ext.com.google.common.collect.Multiset;
import org.apache.jena.ext.com.google.common.collect.TreeMultiset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpWalker;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.GZIPOutputStream;

public abstract class Parser {

	String queryInfo = "";
	BufferedReader bf;
	FileWriter fw;
	BufferedWriter bw;
	Multiset<String> uQ = HashMultiset.create();
	public Multiset<String> canonQueries = TreeMultiset.create();
	public ArrayList<String> unsupportedQueriesList = new ArrayList<String>();
	long totalTime = 0;
	String resultPath;
	int totalQueries = 0;
	int supportedQueries = 0;
	int unsupportedQueries = 0;
	int badSyntaxQueries = 0;
	int interruptedExceptions = 0;
	int otherUnspecifiedExceptions = 0;
	int numberOfDuplicates = 0;
	boolean enableCanonical = true;
	boolean enableLeaning = true;
	boolean verbose = false;
	protected boolean pathNormalisation = false;
	protected boolean enableRewrite = true;

	public Parser(String resultPath) {
		this.resultPath = resultPath;
	}

	public abstract void parse(final String s) throws Exception;

	public void read(File f, File out, int upTo, boolean enableLeaning, boolean enableCanon, boolean enableRewrite) throws IOException{
		read(f, out, upTo, 0, enableLeaning, enableCanon, enableRewrite);
	}

	public void read(File f, File out, int upTo, int offset, boolean enableLeaning, boolean enableCanon, boolean enableRewrite) throws IOException{
		read(f,out,upTo,offset,enableLeaning,enableCanon,enableRewrite,false);
	}

	private void enableRewrite(boolean enableRewrite) {
		this.enableRewrite = enableRewrite;
	}

	public void read(File f, File out, int upTo, int offset, boolean enableLeaning, boolean enableCanon, boolean enableRewrite, boolean paths) throws IOException{
		String s;
		int i = 0;
		this.pathNormalisation = paths;
		this.enableLeaning(enableLeaning);
		this.enableCanonicalisation(enableCanon);
		this.enableRewrite(enableRewrite);
		try {
			bf = new BufferedReader(new FileReader(f));
			fw = new FileWriter(out);
			bw = new BufferedWriter(fw);
			long t = System.currentTimeMillis();
			while ((s = bf.readLine())!=null){
				if (i == upTo){
					break;
				}
				if (i % 1000 == 0){
					System.out.println(i + " queries read.");
				}
				if (i < offset){
					i++;
					continue;
				}
				try{
					if (i == 0){
						@SuppressWarnings("unused")
						SingleQuery q = new SingleQuery(s, enableCanonical, enableRewrite, enableLeaning, paths, verbose);
					}
					this.parse(s);
				}
				catch (UnsupportedOperationException e){
					unsupportedQueries++;
					Query q = QueryFactory.create(s);
					canonQueries.add(q.toString());
					unsupportedQueriesList.add(e.getMessage() + ": " +s);
				}
				catch(QueryParseException e){
					badSyntaxQueries++;
					unsupportedQueriesList.add("Bad syntax: "+s);
				}
				catch (Exception e) {
					otherUnspecifiedExceptions++;
					unsupportedQueriesList.add(s + ":" +e.getMessage());
				}
				totalQueries++;
				i++;
			}
			this.totalTime = System.currentTimeMillis() - t;
			bw.write(getQueryInfo());
			bw.close();
			bf.close();
			this.outputUnsupportedQueries();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void readGZFile(File f, File out, int upTo, int offset, boolean enableLeaning, boolean enableCanon, boolean enableRewrite, boolean paths) throws IOException{
		String s;
		int i = 0;
		this.pathNormalisation = paths;
		this.enableLeaning(enableLeaning);
		this.enableCanonicalisation(enableCanon);
		this.enableRewrite(enableRewrite);
		try {
			FileInputStream fileInputStream = new FileInputStream(f);
			GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
//			TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
//			TarArchiveEntry tarArchiveEntry = null;
//			if ((tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
//				bf = new BufferedReader(new InputStreamReader(tarArchiveInputStream));
//			}
//			else {
//				System.exit(-1);
//			}
			bf = new BufferedReader(new InputStreamReader(gzipInputStream));
			OutputStream os = Files.newOutputStream(out.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			GZIPOutputStream gzip = new GZIPOutputStream(os);
			OutputStreamWriter ow = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
			bw = new BufferedWriter(ow);
			long t = System.currentTimeMillis();
			while ((s = bf.readLine())!=null){
				if (i == upTo){
					break;
				}
				if (i % 1000 == 0){
					System.out.println(s);
					System.out.println(i + " queries read.");
				}
				if (i < offset){
					i++;
					continue;
				}
				try{
					if (i == 0){
						@SuppressWarnings("unused")
						SingleQuery q = new SingleQuery(s, enableCanonical, enableRewrite, enableLeaning, paths, verbose);
					}
					this.parse(s);
				}
				catch (UnsupportedOperationException e){
					unsupportedQueries++;
					Query q = QueryFactory.create(s);
					canonQueries.add(q.toString());
					unsupportedQueriesList.add(e.getMessage() + ": " +s);
				}
				catch(QueryParseException e){
					badSyntaxQueries++;
					unsupportedQueriesList.add("Bad syntax: "+s);
				}
				catch (Exception e) {
					otherUnspecifiedExceptions++;
					unsupportedQueriesList.add(s + ":" +e.getMessage());
				}
				totalQueries++;
				i++;
			}
			this.totalTime = System.currentTimeMillis() - t;
			bw.write(getQueryInfo());
			bw.close();
			bf.close();
			this.outputUnsupportedQueries();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeQueries(String path) throws IOException{
		String s;
		try {
			File f = new File(path);
			BufferedReader bf = new BufferedReader(new FileReader(f));
			File out = new File("clean_" + f.getPath());
			if (!out.exists()){
				out.createNewFile();
			}
			FileWriter fw = new FileWriter(out);
			BufferedWriter bw = new BufferedWriter(fw);
			while ((s = bf.readLine())!=null){
				try{
					Query q = QueryFactory.create(s);
					Op op = Algebra.compile(q);
					q = OpAsQuery.asQuery(op);
					bw.append(s);
					bw.newLine();
				}
				catch (Exception e){
					continue;
				}
			}
			bf.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void removeSPARQL10Queries(String path) throws IOException{
		String s;
		try {
			File f = new File(path);
			BufferedReader bf = new BufferedReader(new FileReader(f));
			File out = new File("sparql11_" + f.getPath());
			if (!out.exists()){
				out.createNewFile();
			}
			FileWriter fw = new FileWriter(out);
			BufferedWriter bw = new BufferedWriter(fw);
			while ((s = bf.readLine())!=null){
				try{
					Query q = QueryFactory.create(s);
					Op op = Algebra.compile(q);
					FeatureCounter fc = new FeatureCounter(op);
					OpWalker.walk(op,fc);
					q = OpAsQuery.asQuery(op);
					boolean paths = fc.getContainsPaths();
					boolean minus = fc.getFeatures().contains("minus");
					boolean extend = fc.getFeatures().contains("extend");
					boolean group = (fc.getFeatures().contains("group"));
					boolean table = (fc.getFeatures().contains("table"));
					if (paths || minus || extend || group || table) {
						bw.append(s);
						bw.newLine();
					}
				}
				catch (Exception e){
					continue;
				}
			}
			bf.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String unsupportedFeaturesToString(){
		String output = "";
		for (Multiset.Entry<String> f : this.uQ.entrySet()){
			output += (f.getElement() + " : " + f.getCount())+"\n";
		}
		return output;
	}
	
	public void enableCanonicalisation(boolean b){
		this.enableCanonical = b;
	}
	
	public void enableLeaning(boolean b){
		this.enableLeaning = b;
	}

	public void getDistributionInfo(String filename) throws IOException {
		getDistributionInfo(filename,false);
	}
	
	public void getDistributionInfo(String filename, boolean gZipped) throws IOException{
		int i = 0;
		int max = 0;
		File file = new File(resultPath + filename + "_dist"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".log");
		BufferedWriter bw;
		if (gZipped) {
			OutputStream os = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			GZIPOutputStream gzip = new GZIPOutputStream(os);
			OutputStreamWriter ow = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
			bw = new BufferedWriter(ow);
		}
		else {
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
		}
		bw.append("Distribution of canonicalised queries: \n");
		bw.newLine();
		for (Multiset.Entry<String> f : canonQueries.entrySet()){
			i++;
			bw.append((f.getElement() + " : " + f.getCount()) + "\n");
			bw.newLine();
			numberOfDuplicates = numberOfDuplicates + (f.getCount() - 1);
			if (f.getCount() > max){
				max = f.getCount();
			}
			if (i%1000 == 0){
				System.out.println(i+" queries added.");
			}
		}
		bw.append("Total number of duplicates detected: "+numberOfDuplicates+"\n");
		bw.append("Most duplicates found: "+max);
		bw.flush();
		bw.close();
	}
	
	public String getQueryInfo(){
		String output = "";
		output += "Total number of queries: " + this.totalQueries + "\n";
		output += "Number of canonicalised queries: " + this.supportedQueries + "\n";
		output += "Number of unique queries: " + this.canonQueries.entrySet().size() + "\n";
		output += "Number of duplicates detected: "+numberOfDuplicates+"\n";
		output += "Number of queries with unsupported features: "+this.unsupportedQueries + "\n";
		output += "Number of queries with unspecified exceptions: "+this.otherUnspecifiedExceptions + "\n";
		output += "Number of queries with bad syntax: "+this.badSyntaxQueries + "\n";
		output += "Number of timeouts: "+this.interruptedExceptions + "\n";
		output += "Summary of unsupported features: \n" + unsupportedFeaturesToString() + "\n";	
		output += "Total elapsed time (in milliseconds) : " + this.totalTime;
		return output;
	}
	
	public void printUnsupportedFeatures(){
		System.out.println(this.unsupportedFeaturesToString());
	}
	
	public void outputUnsupportedQueries() throws IOException{
		if (!unsupportedQueriesList.isEmpty()){
			FileWriter fw = new FileWriter(new File(resultPath + "unsupported"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".log"));
			BufferedWriter bw = new BufferedWriter(fw);
			for (String s : unsupportedQueriesList){
				bw.append(s);
				bw.newLine();
				bw.flush();
			}
			bw.close();
		}
	}
}
