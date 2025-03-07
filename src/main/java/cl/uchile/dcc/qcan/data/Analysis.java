package cl.uchile.dcc.qcan.data;

import com.google.common.primitives.Doubles;
import org.apache.commons.cli.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.jena.atlas.lib.Pair;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Analysis {
	File file;
	BufferedReader br;
	ArrayList<ArrayList<Double>> data = new ArrayList<>();
	ArrayList<Double> average = new ArrayList<>();
	ArrayList<Double> median = new ArrayList<>();
	int uniqueQueries = 0;
	ArrayList<Double> MAX = new ArrayList<>();
	ArrayList<Double> MIN = new ArrayList<>();
	ArrayList<Double> q25 = new ArrayList<>();
	ArrayList<Double> q75 = new ArrayList<>();
	ArrayList<Double> nVars = new ArrayList<>();
	ArrayList<Double> graphSize = new ArrayList<>();
	ArrayList<Double> triples = new ArrayList<>();
	public int distinct = 0;
	public int joins = 0;
	public int unions = 0;
	public int optional = 0;
	public int filter = 0;
	public int solutionMods = 0;
	public int namedGraphs = 0;
	private int bind = 0;
	private int groupBy = 0;
	private int minus = 0;
	private int paths = 0;
	private int values = 0;

	public Analysis(String s) throws IOException {
		this(s,false);
	}

	public Analysis(String s, boolean gZipped) throws IOException {
		this.file = new File(s);
		if (gZipped) {
			FileInputStream fileInputStream = new FileInputStream(this.file);
			GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
			br = new BufferedReader(new InputStreamReader(gzipInputStream));
		}
		else {
			br = new BufferedReader(new FileReader(this.file));
		}

	}
	
	public void read() throws IOException{
		String line = br.readLine();
		if (line.split("\t").length < 5) {
			shortRead();
		}
		else {
			for (int i = 0; i < 8; i++){
				data.add(i, new ArrayList<>());
				average.add(i, 0.0);
				median.add(i,0.0);
				MAX.add(i,0.0);
				MIN.add(i,0.0);
				q25.add(i,0.0);
				q75.add(i,0.0);
			}
			while(true){
				if (line == null || line.startsWith("Total")){
					break;
				}
				else{
					String[] params = line.split("\t");
					for (int i = 0; i < 4; i++){
						double value = Double.parseDouble(params[i+1]);
						if (value > 0) {
							data.get(i).add(value);
						}
					}
//				data.get(2).add(Double.parseDouble(params[4]) - Double.parseDouble(params[6]));
//				data.get(3).add(Double.parseDouble(params[5]) - Double.parseDouble(params[7]));
//				data.get(4).add(Double.parseDouble(params[8]) - Double.parseDouble(params[9]));

					data.get(4).add(Double.parseDouble(params[1])+Double.parseDouble(params[2])+Double.parseDouble(params[3])+Double.parseDouble(params[4]));
					data.get(5).add(Double.parseDouble(params[6])); //triple patterns in
					data.get(6).add(Double.parseDouble(params[7])); //variables in
					data.get(7).add(Double.parseDouble(params[10])); //graph size in
					uniqueQueries++;
					if (params[12].equals("true")){
						distinct++;
					}
					if (params[13].equals("true")){
						joins++;
					}
					if (params[14].equals("true")){
						unions++;
					}
					if (params[15].equals("true")){
						optional++;
					}
					if (params[16].equals("true")){
						filter++;
					}
					if (params[17].equals("true")){
						namedGraphs++;
					}
					if (params[18].equals("true")){
						solutionMods++;
					}
					if (params[19].equals("true")){
						bind++;
					}
					if (params[20].equals("true")){
						groupBy++;
					}
					if (params[21].equals("true")){
						minus++;
					}
					if (params[22].equals("true")){
						paths++;
					}
					if (params[23].equals("true")){
						values++;
					}
					if (uniqueQueries%10000 == 0){
						System.out.println(uniqueQueries + " queries read.");
					}
				}
				line = br.readLine();
			}
			for (ArrayList<Double> datum : data) {
				Collections.sort(datum);
			}
		}

	}
	
	public void shortRead() throws IOException{
		String line;
		for (int i = 0; i < 2; i++){
			data.add(i, new ArrayList<>());
			average.add(i, 0.0);
			median.add(i,0.0);
			MAX.add(i,0.0);
			MIN.add(i,0.0);
			q25.add(i,0.0);
			q75.add(i,0.0);
		}
		while(true){
			line = br.readLine();
			if (line.startsWith("Total")){
				break;
			}
			else{
				String[] params = line.split("\t");

//				data.get(2).add(Double.parseDouble(params[4]) - Double.parseDouble(params[6]));
//				data.get(3).add(Double.parseDouble(params[5]) - Double.parseDouble(params[7]));
//				data.get(4).add(Double.parseDouble(params[8]) - Double.parseDouble(params[9]));
				
				data.get(0).add(Double.parseDouble(params[1]));
				data.get(1).add(Double.parseDouble(params[2]));
				uniqueQueries++;
				if (uniqueQueries%10000 == 0){
					System.out.println(uniqueQueries + " queries read.");
				}
			}
		}
		for (int i = 0; i < data.size(); i++){
			Collections.sort(data.get(i));
		}
	}
	
	public void getAverage(){
		for (int i = 0; i < data.size(); i++){
			for (int k = 0 ; k < data.get(i).size() ; k++){
				double d;
				if (average.isEmpty()){
					d = 0;
				}
				else{
					d = average.get(i);
				}
				average.set(i, d + data.get(i).get(k));
			}
		}	
		for (int i = 0; i < average.size(); i++){
			int n = data.get(i).size();
			if (n > 0) {
				double d = average.get(i)/data.get(i).size();
				average.set(i, d);
			}
		}
	}
	
	public void getStandardDeviation(){
		for (int i = 0; i < data.size(); i++){
			for (double d : data.get(i)){
				double s;
				if (median.isEmpty()){
					s = 0;
				}
				else{
					s = median.get(i);
				}
				median.set(i, s + Math.pow(d - average.get(i), 2));
			}
			double s = median.get(i);
			s = s/uniqueQueries;
			s = Math.sqrt(s);
			median.set(i, s);
		}
	}
	
	public void getMedian(){
		for (int i = 0; i < data.size(); i++){
			int n = data.get(i).size();
			if (n > 0) {
				double median;
				if (n % 2 == 0){
					median = (data.get(i).get(n/2-1) + data.get(i).get(n/2))/2;
				}
				else{
					median = data.get(i).get(n/2);
				}
				this.median.set(i, median);
				this.q25.set(i, data.get(i).get(n/4));
				this.q75.set(i,data.get(i).get(3*n/4));
			}
		}
	}
	
	public void getMax(){
		for (int i = 0; i < data.size(); i++){
			int n = data.get(i).size();
			if (n > 0) {
				double max = data.get(i).get(n-1);
				MAX.set(i, max);
			}
		}
	}
	
	public void getMin(){
		for (int i = 0; i < data.size(); i++){
			if (data.get(i).size() > 0) {
				double min = data.get(i).get(0);
				MIN.set(i, min);
			}
		}
	}
	
	public void getNegativeValues(int param){
		for (int i = 0; i < data.get(param).size(); i++){
			if (data.get(param).get(i) < 0){
				System.out.println(i);
			}
		}
	}
	
	public void displayInfo() throws IOException{
		read();
		getAverage();
		getMedian();
		getMax();
		getMin();
		String s = "";
		if (data.size() < 8) {
			shortDisplayInfo();
		}
		else {
			for (int i = 0; i < data.size(); i++){

				s += average.get(i) + "\t";
				s += median.get(i) + "\t";
				s += q25.get(i) + "\t";
				s += q75.get(i) + "\t";
				s += MAX.get(i) + "\t";
				s += MIN.get(i) + "\n";
			}
			System.out.println(s);
			System.out.println("DISTINCT: "+distinct);
			System.out.println("UNION: "+unions);
			System.out.println("JOIN: "+joins);
			System.out.println("FILTER: "+filter);
			System.out.println("OPTIONAL: "+optional);
			System.out.println("SOLUTION MODIFIERS: "+solutionMods);
			System.out.println("NAMED GRAPH: "+namedGraphs);
			System.out.println("BIND: "+bind);
			System.out.println("GROUP BY: "+groupBy);
			System.out.println("MINUS: "+minus);
			System.out.println("Paths: "+paths);
			System.out.println("VALUES: "+values);
			System.out.println("Total: "+data.get(0).size());
		}

	}
	
	public void shortDisplayInfo() throws IOException{
		String s = "";
		for (int i = 0; i < data.size(); i++){	
			s += average.get(i) + "\t";
			s += median.get(i) + "\t";
			s += q25.get(i) + "\t";
			s += q75.get(i) + "\t";
			s += MAX.get(i) + "\t";
			s += MIN.get(i) + "\n";
		}
		System.out.println(s);
	}

	public void partitionByFeatures() throws IOException {
		String line;
		TreeMap<Integer,List<Double>> map = new TreeMap<>();
		TreeMap<Integer,Integer> nMap = new TreeMap<>();
		for (int i = 0; i < 12; i++){
			map.put(i,new ArrayList<>());
			nMap.put(i,0);
		}
		while (true) {
			line = br.readLine();
			if (line.startsWith("Total")) {
				break;
			}
			else {
				String[] params = line.split("\t");
				boolean distinct = params[12].equals("true");
				boolean joins = params[13].equals("true");
				boolean unions = params[14].equals("true");
				boolean optional = params[15].equals("true");
				boolean filter = params[16].equals("true");
				boolean namedGraphs = params[17].equals("true");
				boolean solutionMods = params[18].equals("true");
				boolean bind = params[19].equals("true");
				boolean groupBy = params[20].equals("true");
				boolean minus = params[21].equals("true");
				boolean paths = params[22].equals("true");
				boolean values = params[23].equals("true");
				boolean[] bools = {distinct, joins, unions, optional, filter, namedGraphs, solutionMods, bind, groupBy, minus, paths, values};
				double totalTime = Double.parseDouble(params[1])+Double.parseDouble(params[2])+Double.parseDouble(params[3])+Double.parseDouble(params[4]);
				totalTime = totalTime / Math.pow(10, 6);
				for (int i = 0; i < bools.length; i++) {
					if (bools[i]) {
						List<Double> prev = map.get(i);
						prev.add(totalTime);
						map.put(i, prev);
						nMap.put(i, nMap.get(i) + 1);
					}
				}

			}
		}
		for (int i = 0; i < map.size(); i++) {
			String s = "";
			switch (i) {
				case 0 : {
					s += "D\t";
					break;
				}
				case 1: {
					s += "J\t";
					break;
				}
				case 2: {
					s += "U\t";
					break;
				}
				case 3: {
					s += "O\t";
					break;
				}
				case 4: {
					s += "F\t";
					break;
				}
				case 5: {
					s += "NG\t";
					break;
				}
				case 6: {
					s += "S\t";
					break;
				}
				case 7: {
					s += "B\t";
					break;
				}
				case 8: {
					s += "G\t";
					break;
				}
				case 9: {
					s += "M\t";
					break;
				}
				case 10: {
					s += "P\t";
					break;
				}
				case 11: {
					s += "V\t";
					break;
				}
			}
			if (nMap.get(i) > 0){
				int n = map.get(i).size();
				List<Double> values = map.get(i);
				Collections.sort(values);
				double max = values.get(values.size()-1);
				double min = values.get(0);
				double avg = 0;
				for (double v : values) {
					avg += v;
				}
				avg = avg/n;
				double median;
				if (n % 2 == 0){
					median = (values.get(n/2-1) + values.get(n/2))/2;
				}
				else{
					median = values.get(n/2);
				}
				double q25 = values.get(n/4);
				double q75 = values.get(3*n/4);
				s += String.format( "%.2f", avg) + "\t";
				s += String.format( "%.2f", median) + "\t";
				s += String.format( "%.2f", q25) + "\t";
				s += String.format( "%.2f", q75) + "\t";
				s += String.format( "%.2f", min) + "\t";
				s += String.format( "%.2f", max) + "\t";
				System.out.println(s);
			}
		}
	}
	
	public void partitionBySetsOfFeatures() throws IOException {
		String line;
		HashMap<List<Boolean>,Double> map = new HashMap<List<Boolean>,Double>();
		HashMap<List<Boolean>,Integer> nMap = new HashMap<List<Boolean>,Integer>();
		while (true) {
			line = br.readLine();
			if (line.startsWith("Total")) {
				break;
			}
			else {
				String[] params = line.split("\t");
				boolean distinct = params[12].equals("true");
				boolean joins = params[13].equals("true");
				boolean unions = params[14].equals("true");
				boolean optional = params[15].equals("true");
				boolean filter = params[16].equals("true");
				boolean namedGraphs = params[17].equals("true");
				boolean solutionMods = params[18].equals("true");
				boolean bind = params[19].equals("true");
				boolean groupBy = params[20].equals("true");
				boolean minus = params[21].equals("true");
				boolean paths = params[22].equals("true");
				boolean values = params[23].equals("true");
				boolean[] bools = {distinct, joins, unions, optional, filter, namedGraphs, solutionMods, bind, groupBy, minus, paths, values};
				List<Boolean> key = new ArrayList<Boolean>();
				for (boolean b : bools) {
					key.add(b);
				}
				double totalTime = Double.parseDouble(params[1])+Double.parseDouble(params[2])+Double.parseDouble(params[3])+Double.parseDouble(params[4]);
				totalTime = totalTime / Math.pow(10, 6);
				if (map.containsKey(key)) {
					map.put(key, map.get(key) + totalTime);
					nMap.put(key, nMap.get(key) + 1);
				}
				else {
					map.put(key, totalTime);
					nMap.put(key, 1);
				}
			}
		}
		List<Pair<List<Boolean>,Double>> entrySet = new ArrayList<>();
		for (Entry<List<Boolean>, Double> entry : map.entrySet()) {
			Pair<List<Boolean>,Double> e = new Pair<>(entry.getKey(), entry.getValue()/nMap.get(entry.getKey()));
			entrySet.add(e);
		}
			Collections.sort(entrySet, new Comparator<Pair<List<Boolean>,Double>>() {

			@Override
			public int compare(Pair<List<Boolean>, Double> o1, Pair<List<Boolean>, Double> o2) {
				return o1.getRight().compareTo(o2.getRight());
			}
		}
			);
		for (Pair<List<Boolean>, Double> entry : entrySet) {
			List<Boolean> key = entry.getLeft();
			String s = "";
			if (key.get(0)) {
				s += "D,";
			}
			if (key.get(1)) {
				s += "J,";
			}
			if (key.get(2)) {
				s += "U,";
			}
			if (key.get(3)) {
				s += "O,";
			}
			if (key.get(4)) {
				s += "F,";
			}
			if (key.get(5)) {
				s += "NG,";
			}
			if (key.get(6)) {
				s += "S,";
			}
			if (key.get(7)) {
				s += "B,";
			}
			if (key.get(8)) {
				s += "G,";
			}
			if (key.get(9)) {
				s += "M,";
			}
			if (key.get(10)) {
				s += "P,";
			}
			if (key.get(11)) {
				s += "V,";
			}
			if (s.length() > 0) {
				s = "{[" + s.substring(0,s.length() - 1) + "]} & " + String.format( "%.2f", map.get(key) / nMap.get(key)) + " & " + nMap.get(key) + " \\\\";
			}
			else {
				s = "{[ ]} & " + String.format( "%.2f", map.get(key) / nMap.get(key)) + " & " + nMap.get(key) + " \\\\";
			}		
			System.out.println(s);
		}
	}
	
	public void filterQueriesByTotalTime(double in) throws IOException {
		String line;
		int uniqueQueries = 0;
		while(true){
			line = br.readLine();
			if (line == null || line.startsWith("Total")){
				break;
			}
			else{
				String[] params = line.split("\t");
				double totalTime = 	Double.parseDouble(params[1])+Double.parseDouble(params[2])+Double.parseDouble(params[3])+Double.parseDouble(params[4]);
				if (in < totalTime) {
					System.out.println(params[0]);
				}
				if (uniqueQueries%10000 == 0){
					System.out.println(uniqueQueries + " queries read.");
				}
			}
			uniqueQueries++;
		}
	}
	
	public void compareWith(Analysis a) throws IOException {
		BufferedReader bf = this.br;
		BufferedReader bf1 = a.br;
		String currentQuery = "";
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		HashMap<String,Integer> map1 = new HashMap<String,Integer>();
		int total1 = 0;
		int total2 = 0;
		while(true){
			String line = bf.readLine();
			if (line.startsWith("Total")){
				break;
			}
			else if (line.startsWith("Distribution") || line.isEmpty()) {
				continue;
			}
			else if (line.startsWith(" :")) {
				String n = line.substring(line.indexOf(" :") + 2).trim();
				try {
					int number = Integer.parseInt(n);
					if (number > 1) {
						map.put(currentQuery, number);
						total1 += number - 1;
					}
				} catch (NumberFormatException e) {
					System.err.print(line);
					System.exit(0);
				}
				
				currentQuery = "";
			}
			else{
				currentQuery += line;
			}
		}
		System.out.println("Done 1");
		while(true){
			String line = bf1.readLine();
			if (line.startsWith("Total")){
				break;
			}
			else if (line.startsWith("Distribution") || line.isEmpty()) {
				continue;
			}
			else if (line.startsWith(" :")) {
				String n = line.substring(line.indexOf(":") + 2).trim();
				try {
					int number = Integer.parseInt(n);
					if (number > 1) {
						map1.put(currentQuery, number);
						total2 += number - 1;
					}
				} catch (NumberFormatException e) {
					System.err.print(line);
					System.exit(0);
				}
				currentQuery = "";
			}
			else{
				currentQuery += line;
			}
		}
		Collection<Integer> c = map.values();
		HashMap<Integer,Integer> valuesMap = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> valuesMap1 = new HashMap<Integer,Integer>();
		for (int i : c) {
			if (valuesMap.containsKey(i)) {
				valuesMap.put(i, valuesMap.get(i) + 1);
			}
			else {
				valuesMap.put(i, 1);
			}
		}
		Collection<Integer> c1 = map1.values();
		for (int i : c1) {
			if (valuesMap1.containsKey(i)) {
				valuesMap1.put(i, valuesMap1.get(i) + 1);
			}
			else {
				valuesMap1.put(i, 1);
			}
		}
		for (Entry<Integer, Integer> entry : valuesMap.entrySet()) {
			if (!valuesMap1.containsKey(entry.getKey())) {
				System.out.println(entry);
			}
			else {
				int value = entry.getValue();
				int value1 = valuesMap1.get(entry.getKey());
				if (value != value1) {
					System.out.println(entry + "\t" + valuesMap1.get(entry.getKey()));
				}
			}
		}
		System.out.println();
		for (Entry<Integer, Integer> entry : valuesMap1.entrySet()) {
			if (!valuesMap.containsKey(entry.getKey())) {
				System.out.println(entry);
			}
		}
		System.out.println("Total 1: " + total1);
		System.out.println("Total 2: " + total2);
	}

	public void plot(int x, int y) throws IOException {
		String line;
		Map<Double,Double> points = new TreeMap<>();
		while(true){
			line = br.readLine();
			if (line == null || line.startsWith("Total")){
				break;
			}
			else{
				String[] params = line.split("\t");
				points.put(Double.parseDouble(params[x]),Double.parseDouble(params[y]));
				uniqueQueries++;
				if (uniqueQueries%10000 == 0){
					System.out.println(uniqueQueries + " queries read.");
				}
			}
		}
		double[] xPoints = Doubles.toArray(points.keySet());
		double[] yPoints = Doubles.toArray(points.values());
	}
	
	public static void main(String[] args) throws IOException{
		CommandLine commandLine;
		Option option_W = new Option("d", false, "Display summary of results.");
		Option option_C = new Option("f", false, "Show summary of results partitioned by features.");
		Option option_N = new Option("s", false, "Show summary of results partitioned by sets of features");
		Option option_X = new Option("x", true, "Path to file containing results.");
		Option option_G = new Option("g",false,"Set if file is gzipped.");
		Options options = new Options();
		CommandLineParser parser = new DefaultParser();

		options.addOption(option_C);
		options.addOption(option_W);
		options.addOption(option_N);
		options.addOption(option_X);
		options.addOption(option_G);

		String header = "";
		String footer = "";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("analysis", header, options, footer, true);
		try{
			commandLine = parser.parse(options, args);
			if (commandLine.hasOption("x")){
				String file = commandLine.getOptionValue("x");
				Analysis analysis = new Analysis(file,commandLine.hasOption("g"));
				if (commandLine.hasOption("d")) {
					analysis.displayInfo();
				}
				else if (commandLine.hasOption("f")) {
					analysis.partitionByFeatures();
				}
				else if (commandLine.hasOption("s")) {
					analysis.partitionBySetsOfFeatures();
				}
				System.exit(0);
			}
		}
		catch (ParseException exception){
			System.out.print("Parse error: ");
			System.out.println(exception.getMessage());
		}

	}

}
