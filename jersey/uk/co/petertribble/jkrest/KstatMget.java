package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("mget/{module}/{instance}/{namespecifier}")

     /*
      * 
      */

public class KstatMget {

    static final JKstat jkstat = new NativeJKstat();

    @GET
    @Produces("application/json")
    public String getKstats(@PathParam("module") String module,
			   @PathParam("instance") String instance,
			   @PathParam("namespecifier") String name) {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	boolean first = true;
	for (String iname : name.split(";")) {
	    KstatFilter ksf = new KstatFilter(jkstat);
	    if ("*".equals(instance)) {
		ksf.addFilter(module + "::" + iname);
	    } else {
		ksf.addFilter(module + ":" + instance + ":" + iname);
	    }
	    // split separate lists
	    if (first) {
		first = false;
	    } else {
		sb.append(",");
	    }
	    sb.append("\"").append(iname).append("\":[");
	    boolean kfirst = true;
	    for (Kstat ks : ksf.getKstats()) {
		if (kfirst) {
		    kfirst = false;
		} else {
		    sb.append(",");
		}
		ks = jkstat.getKstat(ks);
		sb.append(ks.toJSON());
	    }
	    sb.append("]");
	}
	sb.append("}");
	return sb.toString();
    }
}
