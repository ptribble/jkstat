package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("get/{module}/{instance}/{name}")

public class KstatGet {

    static final JKstat jkstat = new NativeJKstat();

    @GET
    @Produces("application/json")
    public String getKstat(@PathParam("module") String module,
			   @PathParam("instance") String instance,
			   @PathParam("name") String name) {
	Kstat ks = jkstat.getKstat(module, Integer.parseInt(instance), name);
	return ks.toJSON();
    }
}
