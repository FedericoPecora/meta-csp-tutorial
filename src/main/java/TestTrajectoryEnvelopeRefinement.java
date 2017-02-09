
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.Map;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.spatioTemporal.paths.Trajectory;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.utility.UI.JTSDrawingPanel;

public class TestTrajectoryEnvelopeRefinement {
	
	public static void main(String[] args) {
		
		TrajectoryEnvelopeScheduler metaSolver = new TrajectoryEnvelopeScheduler(0, 100000);
		TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
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

		//Create a meta-constraint of type Map
		//(needed here because the meta-solver does refinement)
		Map map = new Map(null, null);
		metaSolver.addMetaConstraint(map);

		JTSDrawingPanel.drawConstraintNetwork("Geometries before refinement",solver.getConstraintNetwork());

		//Refine trajectory envelopes so that we get better granularity
		//  -- refinement divides envelopes in spatial overlap areas
		//  -- refinement preserves large envelopes where possible
		ConstraintNetwork refined1 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("Refinement added "+  refined1.getVariables().length  + " variables and " + refined1.getConstraints().length + " constraints");

		//This should do nothing
		ConstraintNetwork refined2 = metaSolver.refineTrajectoryEnvelopes();
		System.out.println("Refinement added "+  refined2.getVariables().length  + " variables and " + refined2.getConstraints().length + " constraints");
		
		JTSDrawingPanel.drawConstraintNetwork("Geometries after refinement",solver.getConstraintNetwork());
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		System.out.println("\n===================\n== VARIABLE INFO ==\n===================");
		System.out.println("\n" + var0.getInfo());
		System.out.println("\n" + var1.getInfo());
		System.out.println("\n" + var2.getInfo());
	}
	
}
