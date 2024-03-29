/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
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
package planning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.simplePlanner.SimplePlannerInferenceCallback;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.sensing.Sensor;
import org.metacsp.time.Bounds;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestProactivePlanningAndDispatching {	
	
	public static void main(String[] args) {

		//Create planner
		SimplePlanner planner = new SimplePlanner(0,100000,0);
		//MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);

		SimpleDomain.parseDomain(planner, "domains/testProactivePlanningLucia.ddl", ProactivePlanningDomain.class);

		ActivityNetworkSolver ans = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];
		SimplePlannerInferenceCallback cb = new SimplePlannerInferenceCallback(planner);
		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(ans, 1000, cb);
		
		final Vector<SymbolicVariableActivity> executingActs = new Vector<SymbolicVariableActivity>();
		DispatchingFunction df = new DispatchingFunction("Robot") {
			@Override
			public void dispatch(SymbolicVariableActivity act) {
				System.out.println(">>>>>>>>>>>>>> Dispatched " + act);
				executingActs.add(act);
			}
			@Override
			public boolean skip(SymbolicVariableActivity act) {
				// TODO Auto-generated method stub
				return false;
			}
		}; 
		
		animator.addDispatchingFunctions(df);
		
		Sensor sensorA = new Sensor("Location", animator);
		Sensor sensorB = new Sensor("Stove", animator);
		
		sensorA.registerSensorTrace("sensorTraces/location.st");
		sensorB.registerSensorTrace("sensorTraces/stove.st");
		
		//TimelinePublisher tp = new TimelinePublisher((ActivityNetworkSolver)planner.getConstraintSolvers()[0], new Bounds(0,60000), true, "Time", "Location", "Stove", "Human", "Robot");
		TimelinePublisher tp = new TimelinePublisher(planner.getConstraintSolvers()[0].getConstraintNetwork(), new Bounds(0,60000), true, "Time", "Location", "Stove", "Human", "Robot");
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(1000);
		
		while (true) {
			System.out.println("Executing activities (press <enter> to refresh list):");
			for (int i = 0; i < executingActs.size(); i++) System.out.println(i + ". " + executingActs.elementAt(i));
			System.out.println("--");
			System.out.print("Please enter activity to finish: ");  
			String input = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
			try { input = br.readLine(); }
			catch (IOException e) { e.printStackTrace(); }
			if (!input.trim().equals("")) {
				try {
					df.finish(executingActs.elementAt(Integer.parseInt(input)));
					executingActs.remove(Integer.parseInt(input));
				}
				catch (ArrayIndexOutOfBoundsException e1) { /* Ignore unknown activity */ }
			}
		}

	}
	
	

}
