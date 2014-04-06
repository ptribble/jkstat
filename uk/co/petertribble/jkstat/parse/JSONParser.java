/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

package uk.co.petertribble.jkstat.parse;

import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.KstatData;
import java.io.*;
import java.util.*;
import org.json.*;

/**
 * Read in JSON serialized kstat output and parse it.
 *
 * @author Peter Tribble
 */
public class JSONParser {

    private Set <Kstat> kstats;

    /**
     * Parse kstat JSON transfer format.
     *
     * @param is the {@code InputStream} to parse
     */
    public JSONParser(InputStream is) {
	parse(new BufferedReader(new InputStreamReader(is)));
    }

    /*
     * Parse kstat JSON transfer format. This is an array of Kstats. Then each
     * Kstat has objects describing the metadata and data: the data object
     * contains the objects describing the statistics and their values.
     */
    private void parse(BufferedReader in) {
	kstats = new HashSet <Kstat> ();
	try {
	    JSONArray ja = new JSONArray(new JSONTokener(in));
	    for (int i = 0; i < ja.length(); i++) {
		kstats.add(getKstat(ja.getJSONObject(i)));
	    }
	} catch (JSONException jse) {
	    System.err.println("ERROR");
	    System.err.println(jse);
	}
    }

    /**
     * Parse the supplied String (in JSON format) and return the encoded
     * Kstat.
     *
     * @param s a String in JSON format representing a Kstat
     *
     * @return the Kstat encoded by the supplied String
     */
    public static Kstat getKstat(String s) {
	try {
	    return getKstat(new JSONObject(s));
	} catch (JSONException jse) {
	    try {
		/*
		 * It may be wrapped in a single-element array. For example,
		 * node returns it this way.
		 */
		JSONArray ja = new JSONArray(s);
		return getKstat(ja.getJSONObject(0));
	    } catch (JSONException jse2) {
		return null;
	    }
	}
    }

    /**
     * Parse the supplied String (in JSON format) and return the encoded
     * Set of Kstats.
     *
     * @param s a String in JSON format representing a Set of Kstats
     *
     * @return the Set of Kstats encoded by the supplied String
     */
    public static Set <Kstat> getKstats(String s) {
	try {
	    return getKstats(new JSONArray(s));
	} catch (JSONException jse) {
	    return null;
	}
    }

    private static Set <Kstat> getKstats(JSONArray ja) {
	Set <Kstat> nkstats = new HashSet <Kstat> ();
	try {
	    for (int i = 0; i < ja.length(); i++) {
		JSONObject jo = ja.getJSONObject(i);
		Kstat ks = new Kstat(jo.getString("module"),
				jo.getInt("instance"), jo.getString("name"));
		ks.setStandardInfo(jo.getString("class"), jo.getInt("type"),
				0L, 0L);
		nkstats.add(ks);
	    }
	} catch (JSONException jse) {
	    // on error, return whatever we have
	}
	return nkstats;
    }

    private static Kstat getKstat(JSONObject jo) {
	try {
	    JSONObject jd = jo.getJSONObject("data");
	    Kstat ks = new Kstat(jo.getString("module"), jo.getInt("instance"),
				jo.getString("name"));
	    ks.setStandardInfo(jo.getString("class"), jo.getInt("type"),
				jo.getLong("crtime"), jo.getLong("snaptime"));
	    Iterator it = jd.keys();
	    while (it.hasNext()) {
		String key = (String) it.next();
		Object o = jd.get(key);
		if (o instanceof Number) {
		    ks.addDataObject(key, KstatData.Type.KSTAT_DATA_UINT64,
				((Number) o).longValue());
		} else {
		    ks.addDataObject(key, KstatData.Type.KSTAT_DATA_STRING,
				(String) o);
		}
	    }
	    return ks;
	} catch (JSONException jse) {
	    return null;
	}
    }

    /**
     * Return the parsed kstats.
     *
     * @return the Set of parsed Kstats
     */
    public Set <Kstat> getKstats() {
	return kstats;
    }
}
