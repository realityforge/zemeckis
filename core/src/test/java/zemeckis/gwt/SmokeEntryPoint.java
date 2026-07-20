package zemeckis.gwt;

import com.google.gwt.core.client.EntryPoint;
import zemeckis.Zemeckis;

public final class SmokeEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Zemeckis.areNamesEnabled();
    }
}
