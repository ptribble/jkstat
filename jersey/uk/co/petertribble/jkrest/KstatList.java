package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("list")

public class KstatList {

    static final JKstat jkstat = new NativeJKstat();

    @GET
    @Produces("application/json")
    public String getKstat() {
	KstatSet kss = new KstatSet(jkstat);
	return kss.toJSON();
    }
}
