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
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

import util.Parsing;

public class SimpleDispatchingExample {	
	
	public static void main(String[] args) {

		long origin = Calendar.getInstance().getTimeInMillis();
		//Create ActivityNetworkSolver, origin = current time
		ActivityNetworkSolver ans = new ActivityNetworkSolver(origin,origin+100000);
		MetaCSPLogging.setLevel(ans.getClass(), Level.FINE);

		/*
		 *                     Delivery location
		 *                             x
		 *                             ^
		 *                             | 
		 *                             |
		 *                             |
		 * MiR location                |
		 *      x -------------------> x
		 *                       UR location
		 * 
		 */
		SymbolicVariableActivity goto_ur = (SymbolicVariableActivity)ans.createVariable("MiR");
		goto_ur.setSymbolicDomain("goto_ur");
		SymbolicVariableActivity goto_delivery = (SymbolicVariableActivity)ans.createVariable("MiR");
		goto_delivery.setSymbolicDomain("goto_delivery");
		SymbolicVariableActivity place_obj = (SymbolicVariableActivity)ans.createVariable("UR");
		place_obj.setSymbolicDomain("place_obj");

		//Minimum durations of activities (at least 3 seconds for all activities, just as an example)
		AllenIntervalConstraint goto_ur_min_duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(3000,APSPSolver.INF));
		goto_ur_min_duration.setFrom(goto_ur);
		goto_ur_min_duration.setTo(goto_ur);

		AllenIntervalConstraint goto_delivery_min_duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(3000,APSPSolver.INF));
		goto_delivery_min_duration.setFrom(goto_delivery);
		goto_delivery_min_duration.setTo(goto_delivery);

		AllenIntervalConstraint place_obj_min_duration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(3000,APSPSolver.INF));
		place_obj_min_duration.setFrom(place_obj);
		place_obj_min_duration.setTo(place_obj);

		//Desired temporal relations between activities
		AllenIntervalConstraint goto_meets_place = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		goto_meets_place.setFrom(goto_ur);
		goto_meets_place.setTo(place_obj);

		AllenIntervalConstraint place_before_goto = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
		place_before_goto.setFrom(place_obj);
		place_before_goto.setTo(goto_delivery);
		

		boolean added = ans.addConstraints(
				goto_ur_min_duration,
				goto_delivery_min_duration,
				place_obj_min_duration,
				goto_meets_place,
				place_before_goto
				);

		System.out.println("Constraints consistent? " + added);


		/**
		 * Now let's make this come alive!
		 */

		//Create animator and tell it to animate the ActivityNetworkSolver w/ period 100 msec
		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(ans, 100);

		final DispatchingFunction df_mir = new DispatchingFunction("MiR") {	
			@Override
			public boolean skip(SymbolicVariableActivity act) { return false; }

			@Override
			public void dispatch(SymbolicVariableActivity act) {
				System.out.println("MiR is executing " + act.getSymbols()[0]);
			}
		};

		final DispatchingFunction df_ur = new DispatchingFunction("UR") {	
			@Override
			public boolean skip(SymbolicVariableActivity act) { return false; }

			@Override
			public void dispatch(SymbolicVariableActivity act) {
				System.out.println("UR is executing " + act.getSymbols()[0]);
			}
		};

		animator.addDispatchingFunctions(df_mir,df_ur);
		
		//Visualize progression, with automatic update every 100 msec 
		//Add these args if you have scaling problems: -Dsun.java2d.uiScale=2.5
		TimelinePublisher tp = new TimelinePublisher(ans.getConstraintNetwork(), new Bounds(0,60000), true, "MiR", "UR", "Time");
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(100);
		
		//Poor Man's key listener ;-)
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
				}
				catch(NumberFormatException nfe) { }
				catch(ArrayIndexOutOfBoundsException aiob) { }
			}

		}



	}



}
