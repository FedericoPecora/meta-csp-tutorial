
import java.util.Arrays;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.JTSDrawingPanel;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

public class TestTrajectoryEnvelopeScheduling {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];

		//Another way to specify footprints...
		//2.3 (w) x 6.5 (l) with reference point in the middle (deltas are 0)
		double width = 2.3;
		double length = 6.5;
		double deltaWidth = 0.0;
		double deltaLength = 0.0;

		long currentTime = 0;
		long parkingDuration = 3000;
		int numRobots = 3;
		Variable[] vars = solver.createVariables(3*numRobots);

		for (int i = 0; i < numRobots; i++) {
			TrajectoryEnvelope driving = (TrajectoryEnvelope)vars[i];
			driving.setComponent("Robot" + i);
			driving.getSymbolicVariableActivity().setSymbolicDomain("Driving");
			driving.setRobotID(i);
			Trajectory drivingTraj = new Trajectory("paths/newpath" + (i+1) + ".path");
			driving.setFootprint(width,length,deltaWidth, deltaLength);
			driving.setTrajectory(drivingTraj);

			TrajectoryEnvelope parkingStart = (TrajectoryEnvelope)vars[i+numRobots];
			parkingStart.setComponent("Robot" + i);
			parkingStart.getSymbolicVariableActivity().setSymbolicDomain("Parking (initial)");
			parkingStart.setRobotID(i);
			Trajectory parkingTrajStart = new Trajectory(new Pose[] {driving.getTrajectory().getPose()[0]});
			parkingStart.setFootprint(width,length,deltaWidth,deltaLength);
			parkingStart.setTrajectory(parkingTrajStart);
			parkingStart.setRefinable(false);

			TrajectoryEnvelope parkingFinal = (TrajectoryEnvelope)vars[i+2*numRobots];
			parkingFinal.setComponent("Robot" + i);
			parkingFinal.getSymbolicVariableActivity().setSymbolicDomain("Parking (final)");
			parkingFinal.setRobotID(i);
			Trajectory parkingTrajFinal = new Trajectory(new Pose[] {driving.getTrajectory().getPose()[driving.getPathLength()-1]});
			parkingFinal.setFootprint(width,length,deltaWidth,deltaLength);
			parkingFinal.setTrajectory(parkingTrajFinal);
			parkingFinal.setRefinable(false);

			AllenIntervalConstraint meetsParkingStartDriving = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			meetsParkingStartDriving.setFrom(parkingStart);
			meetsParkingStartDriving.setTo(driving);
			solver.addConstraints(meetsParkingStartDriving);

			AllenIntervalConstraint meetsDrivingParkingFinal = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			meetsDrivingParkingFinal.setFrom(driving);
			meetsDrivingParkingFinal.setTo(parkingFinal);
			solver.addConstraints(meetsDrivingParkingFinal);

			AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(currentTime, currentTime));
			release.setFrom(parkingStart);
			release.setTo(parkingStart);
			solver.addConstraint(release);
			
			AllenIntervalConstraint parkingDurStart = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(parkingDuration, APSPSolver.INF));
			parkingDurStart.setFrom(parkingStart);
			parkingDurStart.setTo(parkingStart);
			solver.addConstraint(parkingDurStart);

			AllenIntervalConstraint parkingFinalForever = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Forever);
			parkingFinalForever.setFrom(parkingFinal);
			parkingFinalForever.setTo(parkingFinal);
			solver.addConstraint(parkingFinalForever);
		}
				
		//Create a meta-constraint of type Map
		//  -- meta-variables are TrajectoryEnvelopes that overlaps in time and space
		//  -- meta-values are BeforeOrMeets constraints that separate in time
		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);
		
		//Refinement of trajectory envelopes allows to separate overlapping envelopes in time
		metaSolver.refineTrajectoryEnvelopes();
		
		JTSDrawingPanel.drawConstraintNetwork("Geometries", solver.getConstraintNetwork());
		ConstraintNetwork.draw(solver.getConstraintNetwork(), "Constraint Network");
		
		//Call the scheduler (backtracking search over assignments of meta-values to meta-variables)
		//  -- in this case, this means search in the space of resolving BeforeOrMeets constraints
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));
		
		//Show everything in GUI that can be animated
		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.setTrajectoryEnvelopeScheduler(metaSolver);
		tea.setTrajectoryEnvelopes(solver.getConstraintNetwork());
	}
	
}
