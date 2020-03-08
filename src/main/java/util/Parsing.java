/*******************************************************************************
 * Copyright (c) 2010-2020 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

public class Parsing {

	private static ActivityNetworkSolver ans = null;
	private static HashMap<String,SymbolicVariableActivity> idsToVars = new HashMap<String, SymbolicVariableActivity>();
	public static String s = null;
	
	public static void setVariableFactory(ActivityNetworkSolver solver) {
		Parsing.ans = solver;
	}

	private static Bounds processBounds(String bounds) {
		String noPar = bounds = bounds.substring(bounds.indexOf("[")+1,bounds.indexOf("]"));
		noPar = noPar.replaceAll("\\s+", "");
		noPar = noPar.replaceAll("\t", "");
		String[] twoNums = noPar.split(",");
		long num1 = -1;
		if (twoNums[0].contains("INF")) {
			if (twoNums[0].startsWith("-")) num1 = -APSPSolver.INF;
			else num1 = APSPSolver.INF;
		}
		else num1 = Integer.parseInt(twoNums[0]);
		long num2 = -1;
		if (twoNums[1].contains("INF")) {
			if (twoNums[1].startsWith("-")) num2 = -APSPSolver.INF;
			else num2 = APSPSolver.INF;
		}
		else num2 = Integer.parseInt(twoNums[1]);
		return new Bounds(num1,num2);
	}

	public static AllenIntervalConstraint makeConstraint(List<Object> objs) {
		//(Constraint type varFromId varToId [lb,ub] ... [lb,ub])
		String type_s = (String)objs.get(1);
		AllenIntervalConstraint.Type type = AllenIntervalConstraint.Type.fromString(type_s);
		SymbolicVariableActivity actFrom = idsToVars.get((String)objs.get(2));
		SymbolicVariableActivity actTo = idsToVars.get((String)objs.get(3));
		ArrayList<Bounds> bounds = new ArrayList<Bounds>();
		for (int i = 4; i < objs.size(); i++) bounds.add(processBounds((String)objs.get(i)));
		AllenIntervalConstraint con = null;
		if (bounds.isEmpty()) con = new AllenIntervalConstraint(type);
		else con = new AllenIntervalConstraint(type, bounds.toArray(new Bounds[bounds.size()]));
		con.setFrom(actFrom);
		con.setTo(actTo);
		return con;
	}
	
	public static SymbolicVariableActivity makeVariable(List<Object> objs) {
		String id = (String)objs.get(1);
		String[] values = new String[objs.size()-3];
		for (int i = 2; i < objs.size()-1; i++) values[i-2] = (String)objs.get(i);
		String component = (String)objs.get(objs.size()-1);
		SymbolicVariableActivity act = (SymbolicVariableActivity)ans.createVariable(component);
		act.setSymbolicDomain(values);
		Parsing.idsToVars.put(id, act);
		return act;
	}
	
	public static ConstraintNetwork parseSpecification(String spec) {
		ConstraintNetwork ret = new ConstraintNetwork(null);
		MultiListParser parser = new MultiListParser(spec);
		List<Object> objs = parser.parseObjects();
		System.out.println(objs);
		for (int i = 0; i < objs.size(); i++) {
			@SuppressWarnings("unchecked")
			List<Object> oneline = (List<Object>)objs.get(i);
			if (oneline.get(0) instanceof String) {
				String onelinetype = (String)oneline.get(0);
				if (onelinetype.equals("Constraint")) ret.addConstraint(makeConstraint(oneline));
				else if (onelinetype.equals("Variable")) ret.addVariable(makeVariable(oneline));
			}
		}
		return ret;
	}
	
	public static ConstraintNetwork loadSpecification(String filename) {
		String spec = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			for(String line; (line = br.readLine()) != null; ) {
				spec += (line);
			}
			br.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		return parseSpecification(spec);
	}
	
	
}
