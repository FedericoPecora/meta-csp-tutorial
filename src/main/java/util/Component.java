package util;

import java.util.HashSet;

import org.metacsp.multi.activity.SymbolicVariableActivity;

public abstract class Component {
	
	public static enum STATE {_NONE, IDLE, ACTING};
	
	private String name = null;
	private STATE state = STATE._NONE;
	private HashSet<SymbolicVariableActivity> currentActivities = new HashSet<SymbolicVariableActivity>();
	
	public Component(String name) {
		this.name = name;
	}
	
	public ActivityCallback startActivity(SymbolicVariableActivity act) {
		this.state = STATE.ACTING;
		this.currentActivities.add(act);
		return this.doStart(act);
	}
	
	public void finishActivity(SymbolicVariableActivity act) {
		this.currentActivities.remove(act);
		if (this.currentActivities.isEmpty()) this.state = STATE.IDLE;
	}
	
	public SymbolicVariableActivity[] getActivitiesInExecution() {
		return this.currentActivities.toArray(new SymbolicVariableActivity[currentActivities.size()]);
	}
	
	@Override
	public String toString() {
		String ret = "[" + this.name + "] State: " + this.state;
		if (this.state.equals(STATE.ACTING)) ret += ", " + this.currentActivities.size() + " activities in execution";
		return ret;
	}
	
	public abstract ActivityCallback doStart(final SymbolicVariableActivity act);

}
