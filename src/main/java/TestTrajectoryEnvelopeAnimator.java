
import java.util.Arrays;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.TrajectoryEnvelopeAnimator;

public class TestTrajectoryEnvelopeAnimator {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
		Variable[] vars = solver.createVariables(3);
		TrajectoryEnvelope var0 = (TrajectoryEnvelope)vars[0];
		TrajectoryEnvelope var1 = (TrajectoryEnvelope)vars[1];
		TrajectoryEnvelope var2 = (TrajectoryEnvelope)vars[2];
		
		Trajectory traj0 = new Trajectory("paths/newpath1.path");
		var0.setFootprint(1.3, 3.5, 0.0, 0.0);
		var0.setTrajectory(traj0);

		Trajectory traj1 = new Trajectory("paths/newpath2.path");
		var1.setFootprint(1.3, 3.5, 0.0, 0.0);
		var1.setTrajectory(traj1);

		Trajectory traj2 = new Trajectory("paths/newpath3.path");
		var2.setFootprint(1.3, 3.5, 0.0, 0.0);
		var2.setTrajectory(traj2);
		
		var0.setRobotID(1);
		var1.setRobotID(2);
		var2.setRobotID(3);
		
		System.out.println(var0 + " has domain " + var0.getDomain());
		System.out.println(var1 + " has domain " + var1.getDomain());
		System.out.println(var2 + " has domain " + var2.getDomain());
		
		Map map = new Map(null, null);		
		metaSolver.addMetaConstraint(map);
		
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 1: "+  refined1);

		ConstraintNetwork refined2 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("REFINED 2: "+  refined2);
		
		boolean solved = metaSolver.backtrack();
		System.out.println("Solved? " + solved);
		if (solved) System.out.println("Added resolvers:\n" + Arrays.toString(metaSolver.getAddedResolvers()));

		TrajectoryEnvelopeAnimator tea = new TrajectoryEnvelopeAnimator("This is a test");
		tea.addTrajectoryEnvelopes(var0, var1, var2);

//		JTSDrawingPanel.drawConstraintNetwork("Geometries after refinement",refined1);
//		ConstraintNetwork.draw(solver.getConstraintNetwork());

	}
	
}
