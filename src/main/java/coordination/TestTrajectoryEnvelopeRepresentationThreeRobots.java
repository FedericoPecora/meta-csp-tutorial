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
package coordination;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.JTSDrawingPanel;

public class TestTrajectoryEnvelopeRepresentationThreeRobots {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeSolver solver = new TrajectoryEnvelopeSolver(0, 100000);
		Variable[] vars = solver.createVariables(3);
		
		//Parking envelopes not created for simplicity
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		var0.setComponent("Robot1");
		var0.getSymbolicVariableActivity().setSymbolicDomain("Driving");
		var0.setRobotID(1);

		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		var1.setComponent("Robot2");
		var1.getSymbolicVariableActivity().setSymbolicDomain("Driving");
		var1.setRobotID(2);
		
		TrajectoryEnvelope var2 = (TrajectoryEnvelope)vars[2];
		var2.setComponent("Robot3");
		var2.getSymbolicVariableActivity().setSymbolicDomain("Driving");
		var2.setRobotID(3);

		//Another way to specify footprints
		//2.3 (w) x 6.5 (l) centered in the middle (deltas are 0)
		double width = 2.3;
		double length = 6.5;
		double deltaWidth = 0.0;
		double deltaLength = 0.0;
		
		Trajectory traj0 = new Trajectory("paths/newpath1.path");
		var0.setFootprint(width,length,deltaWidth, deltaLength);
		var0.setTrajectory(traj0);

		Trajectory traj1 = new Trajectory("paths/newpath2.path");
		var1.setFootprint(width,length,deltaWidth, deltaLength);
		var1.setTrajectory(traj1);

		Trajectory traj2 = new Trajectory("paths/newpath3.path");
		var2.setFootprint(width,length,deltaWidth, deltaLength);
		var2.setTrajectory(traj2);
						
		JTSDrawingPanel.drawConstraintNetwork("Geometries", solver.getConstraintNetwork());
		ConstraintNetwork.draw(solver.getConstraintNetwork(), "Constraint Network");
		
		System.out.println("\n===================\n== VARIABLE INFO ==\n===================");
		System.out.println("\n" + var0.getInfo());
		System.out.println("\n" + var1.getInfo());
		System.out.println("\n" + var2.getInfo());
	}
	
}
