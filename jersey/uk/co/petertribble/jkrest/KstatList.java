package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("list")

public class KstatList {

    static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Get the list of current kstats. Just retunrs metadat, without any data.
     *
     * @return a JSON formatted list of kstats
     */
    @GET
    @Produces("application/json")
    public String getKstat() {
	KstatSet kss = new KstatSet(JKSTAT);
	return kss.toJSON();
    }
}
