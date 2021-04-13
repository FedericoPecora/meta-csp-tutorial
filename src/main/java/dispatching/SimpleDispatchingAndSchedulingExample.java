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
package dispatching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.logging.Level;

import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.meta.symbolsAndTime.StateVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

import util.Parsing;

public class SimpleDispatchingAndSchedulingExample {	
	
	public static void main(String[] args) {

		MetaCSPLogging.setLevel(Level.OFF);
		
		long origin = Calendar.getInstance().getTimeInMillis();
		
		// Create Scheduler, origin = current time
		final Scheduler svs = new Scheduler(origin, origin+1000000, 0);
		
		// Get the Scheduler's underlying ActivityNetworkSolver
		ActivityNetworkSolver ans = (ActivityNetworkSolver)svs.getConstraintSolversFromConstraintSolverHierarchy(ActivityNetworkSolver.class)[0];

		// Parse the specification...
		Parsing.setVariableFactory(ans);
		ConstraintNetwork cn = Parsing.loadSpecification("specification2.txt");
		// ... and add the parsed constraints
		boolean added = ans.addConstraints(cn.getConstraints());
		System.out.println("Constraints consistent? " + added);
		
		// Make a StateVariable for the UR which can be used for scheduling
		// This tells the scheduler that for the UR, it must enforce states that overlap in time
		// must not be conflicting
		StateVariable ur_capacity = new StateVariable(null, null, svs, new String[] {});
		svs.addMetaConstraint(ur_capacity);
		
		//Make sure the scheduler knows about the activities
		for (Variable var : ans.getVariables("UR")) ur_capacity.setUsage((Activity)var);
		
		// Ask the scheduler to make sure that there are no conflicts
		// (solve the scheduling problem by imposing temporal constraints that remove temporal overlap)
		System.out.println("Solved? " + svs.backtrack());

		
		/**
		 * Now let's make this come alive!
		 */

		// Create animator and tell it to animate the ActivityNetworkSolver w/ period 100 msec
		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(ans, 100);
		
		final DispatchingFunction df_mir_1 = new DispatchingFunction("MiR_1") {	
			@Override
			public boolean skip(SymbolicVariableActivity act) { return false; }

			@Override
			public void dispatch(SymbolicVariableActivity act) {
				System.out.println(act.getComponent() + " starts executing " + act.getSymbols()[0]);
			}
		};

		final DispatchingFunction df_mir_2 = new DispatchingFunction("MiR_2") {	
			@Override
			public boolean skip(SymbolicVariableActivity act) { return false; }

			@Override
			public void dispatch(SymbolicVariableActivity act) {
				System.out.println(act.getComponent() + " starts executing " + act.getSymbols()[0]);
			}
		};

		final DispatchingFunction df_ur = new DispatchingFunction("UR") {	
			@Override
			public boolean skip(SymbolicVariableActivity act) { return false; }

			@Override
			public void dispatch(SymbolicVariableActivity act) {
				System.out.println(act.getComponent() + " starts executing " + act.getSymbols()[0]);
			}
		};

		animator.addDispatchingFunctions(df_mir_1,df_mir_2,df_ur);
		
		// Visualize progression, with automatic update every 100 msec 
		// Add these args if you have scaling problems: -Dsun.java2d.uiScale=2
		TimelinePublisher tp = new TimelinePublisher(ans.getConstraintNetwork(), new Bounds(0,60000), true, "MiR_1", "MiR_2", "UR", "Time");
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(100);
		
		// Poor Man's key listener ;-)
		while (true) {			
			System.out.println("Executing activities (press <enter> to refresh list):");
			SymbolicVariableActivity[] acts = animator.getDispatcher().getStartedActs();
			for (int i = 0; i < acts.length; i++) {
				System.out.println(i + ": " + acts[i]);
			}
			System.out.println("--");
			System.out.print("Please enter activity to finish: ");  
			String input = "";  
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
			try { input = br.readLine(); }
			catch (IOException e) { e.printStackTrace(); }
			if (!input.trim().equals("")) {		
				try {
					SymbolicVariableActivity actToFinish = acts[Integer.parseInt(input)];
					DispatchingFunction df = animator.getDispatcher().getDispatchingFunction(actToFinish.getComponent());
					df.finish(actToFinish);
					System.out.println(actToFinish.getComponent() + " finishes executing " + actToFinish.getSymbols()[0]);
				}
				catch(NumberFormatException nfe) { }
				catch(ArrayIndexOutOfBoundsException aiob) { }
			}
		}
	}

}
