package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("getkcid")

public class KstatGetkcid {

    static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Get the current kstat chain id.
     *
     * @return the current chain id, as a String
     */
    @GET
    @Produces("application/json")
    public String getKstat() {
	return Integer.toString(JKSTAT.getKCID());
    }
}
