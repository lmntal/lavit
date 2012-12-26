package lavit.multiedit.coloring.event;

import java.util.EventListener;

public interface DirtyFlagChangeListener extends EventListener
{
	public void dirtyFlagChanged(boolean dirty);
}
