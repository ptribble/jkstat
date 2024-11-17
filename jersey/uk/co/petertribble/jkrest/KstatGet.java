package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("get/{module}/{instance}/{name}")

public class KstatGet {

    static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Get a kstat.
     *
     * @param module the desired module
     * @param instance the desired instance, as a String
     * @param name the desired name
     *
     * @return a JSON formatted kstat
     */
    @GET
    @Produces("application/json")
    public String getKstat(@PathParam("module") String module,
			   @PathParam("instance") String instance,
			   @PathParam("name") String name) {
	Kstat ks = JKSTAT.getKstat(module, Integer.parseInt(instance), name);
	return ks.toJSON();
    }
}
