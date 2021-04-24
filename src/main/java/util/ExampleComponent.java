package util;

import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.multi.activity.SymbolicVariableActivity;

public class ExampleComponent extends Component {

	private DispatchingFunction df = null;
	
	public ExampleComponent(String name, DispatchingFunction df) {
		super(name);
		this.df = df;
	}

	@Override
	public ActivityCallback doStart(final SymbolicVariableActivity act) {

		return new ActivityCallback() {
			@Override
			public Object onFinish() {
				df.finish(act);
				return null;
			}

			@Override
			public Object onStart() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object onProgress() {
				// TODO Auto-generated method stub
				return null;
			}
		};

	}
}
