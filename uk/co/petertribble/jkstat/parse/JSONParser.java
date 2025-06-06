/*
 * SPDX-License-Identifier: CDDL-1.0
 *
 * CDDL HEADER START
 *
 * This file and its contents are supplied under the terms of the
 * Common Development and Distribution License ("CDDL"), version 1.0.
 * You may only use this file in accordance with the terms of version
 * 1.0 of the CDDL.
 *
 * A full copy of the text of the CDDL should have accompanied this
 * source. A copy of the CDDL is also available via the Internet at
 * http://www.illumos.org/license/CDDL.
 *
 * CDDL HEADER END
 *
 * Copyright 2025 Peter Tribble
 *
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

    private Set<Kstat> kstats;

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
	kstats = new HashSet<>();
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
    public static Set<Kstat> getKstats(String s) {
	try {
	    return getKstats(new JSONArray(s));
	} catch (JSONException jse) {
	    return null;
	}
    }

    private static Set<Kstat> getKstats(JSONArray ja) {
	Set<Kstat> nkstats = new HashSet<>();
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

    @SuppressWarnings("rawtypes")
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
    public Set<Kstat> getKstats() {
	return kstats;
    }
}
