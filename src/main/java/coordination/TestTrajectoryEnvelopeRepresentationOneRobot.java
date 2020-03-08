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
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

import com.vividsolutions.jts.geom.Coordinate;

import edu.uci.ics.jung.graph.DelegateTree;

public class TestTrajectoryEnvelopeRepresentationOneRobot {
	
		
	public static void main(String[] args) {
		
		TrajectoryEnvelopeSolver solver = new TrajectoryEnvelopeSolver(0, 1000000, 100);
		Variable[] vars = solver.createVariables(2, "Robot1");
		TrajectoryEnvelope trajEnvelopeRobot1 = (TrajectoryEnvelope)vars[0];		
		TrajectoryEnvelope parkingEnvelopeRobot1 = (TrajectoryEnvelope)vars[1];		
		trajEnvelopeRobot1.getSymbolicVariableActivity().setSymbolicDomain("Drive");
		parkingEnvelopeRobot1.getSymbolicVariableActivity().setSymbolicDomain("Parking");

		//Coordinates of the robot footprint
		//(direction of motion is positive x)
		Coordinate frontLeft = new Coordinate(2.7, 0.7);
		Coordinate frontRight = new Coordinate(2.7, -0.7);
		Coordinate backRight = new Coordinate(-1.7, -0.7);
		Coordinate backLeft = new Coordinate(-1.7, 0.7);

		//Create a driving envelope to represent that the vehicle is moving
		Trajectory trajRobot1 = new Trajectory("paths/path1.path");
		trajEnvelopeRobot1.setFootprint(backLeft,backRight,frontLeft,frontRight);
		trajEnvelopeRobot1.setTrajectory(trajRobot1);
		trajEnvelopeRobot1.setRobotID(1);
		
		//Make a parking envelope to represent that the vehicle is still in the beginning
		Trajectory parkingPoseRobot1 = new Trajectory(new Pose[] {trajEnvelopeRobot1.getTrajectory().getPose()[0]});
		parkingEnvelopeRobot1.setFootprint(backLeft,backRight,frontLeft,frontRight);
		parkingEnvelopeRobot1.setTrajectory(parkingPoseRobot1);
		parkingEnvelopeRobot1.setRobotID(1);
		
		//Parking temporally MEETS driving
		AllenIntervalConstraint meets = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		meets.setFrom(parkingEnvelopeRobot1);
		meets.setTo(trajEnvelopeRobot1);
		solver.addConstraints(meets);

		System.out.println(trajEnvelopeRobot1 + " has domain " + trajEnvelopeRobot1.getDomain());

		//Show variable hierarchy
		DelegateTree<Variable,String> varTree = trajEnvelopeRobot1.getVariableHierarchy();
		MultiVariable.drawVariableHierarchy(varTree);

		//Show solver hierarchy
		DelegateTree<ConstraintSolver,String> csTree = solver.getConstraintSolverHierarchy();
		MultiConstraintSolver.drawConstraintSolverHierarchy(csTree);
		
		//Draw the trajectory envelopes in a GUI
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("TrajecotryEnvelope of " + trajEnvelopeRobot1.getComponent());
		tea.addTrajectoryEnvelopes(parkingEnvelopeRobot1, trajEnvelopeRobot1);

		//Show that there is a temporal constraint network underlying all of this
		//  -- temporal constraint network maintains qualitative temporal relations
		AllenIntervalNetworkSolver temporalSolver = (AllenIntervalNetworkSolver)solver.getConstraintSolversFromConstraintSolverHierarchy(AllenIntervalNetworkSolver.class)[0];
		ConstraintNetwork.draw(temporalSolver.getConstraintNetwork());

		//Show that there is also a spatial constraint network underlying all of this
		//  -- spatial constraint network maintains qualitative spatial relations
		DE9IMRelationSolver spatialSolver = (DE9IMRelationSolver)solver.getConstraintSolversFromConstraintSolverHierarchy(DE9IMRelationSolver.class)[0];
		spatialSolver.addConstraints(spatialSolver.getAllImplicitRCC8Relations());
		ConstraintNetwork.draw(spatialSolver.getConstraintNetwork());
	}
	
}
