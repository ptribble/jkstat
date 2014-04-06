package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("getkcid")

public class KstatGetkcid {

    static final JKstat jkstat = new NativeJKstat();

    @GET
    @Produces("application/json")
    public String getKstat() {
	return Integer.toString(jkstat.getKCID());
    }
}
