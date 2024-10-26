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

import java.util.zip.*;
import java.util.*;
import java.io.IOException;
import uk.co.petertribble.jkstat.api.*;

/**
 * An implementation of the JKstat class that retrieves data from JSON output
 * stored in a zip file. Use the next() and previous() methods to step through
 * the available data.
 *
 * @author Peter Tribble
 */
public class ParseableJSONZipJKstat extends SequencedJKstat {

    private ZipFile zf;
    private ZipEntry[] inputs;
    private boolean cached;

    /*
     * We need a separate cached map for each zipfile.
     */
    private Map <Integer, JSONParser> ppmap;

    /**
     * Constructs a ParseableJSONZipJKstat object, with JSON cacheing disabled
     * by default.
     *
     * @param name the name of the zip file to open
     *
     * @throws IOException if there's a problem opening the zip file
     */
    public ParseableJSONZipJKstat(String name) throws IOException {
	this(new ZipFile(name), new HashMap<>(), false);
    }

    /**
     * Constructs a ParseableJSONZipJKstat object.
     *
     * @param name the name of the zip file to open
     * @param cached whether caching should be enabled
     *
     * @throws IOException if there's a problem opening the zip file
     */
    public ParseableJSONZipJKstat(String name, boolean cached)
		throws IOException {
	this(new ZipFile(name), new HashMap<>(), cached);
    }

    /**
     * Constructs a ParseableJSONZipJKstat object.
     *
     * @param zf the ZipFile containing data
     * @param ppmap a Map containing parsed zip files
     * @param cached whether caching should be enabled
     */
    public ParseableJSONZipJKstat(ZipFile zf,
				Map <Integer, JSONParser> ppmap,
				boolean cached) {
	super();
	this.zf = zf;
	this.ppmap = ppmap;
	this.cached = cached;
	ArrayList <ZipEntry> al = new ArrayList<>();
	for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements(); ) {
	    al.add(e.nextElement());
	}
	inputs = al.toArray(new ZipEntry[0]);
	Arrays.sort(inputs, new Comparator <ZipEntry>() {
			@Override
			public int compare(ZipEntry f1, ZipEntry f2) {
			    if (f1.getTime() > f2.getTime()) {
				return +1;
			    } else if (f1.getTime() < f2.getTime()) {
				return -1;
			    } else {
				return 0;
			    }
			}
		    });
	begin();
    }

    @Override
    public SequencedJKstat newInstance() {
	return new ParseableJSONZipJKstat(zf, ppmap, cached);
    }

    @Override
    public void begin() {
	chainid = 0;
	readFile(0);
    }

    @Override
    public boolean next() {
	if (chainid < inputs.length - 1) {
	    chainid++;
	    readFile(chainid);
	    return true;
	}
	return false;
    }

    @Override
    public boolean previous() {
	if (chainid > 0) {
	    chainid--;
	    readFile(chainid);
	    return true;
	}
	return false;
    }

    @Override
    public int size() {
	return inputs.length;
    }

    /*
     * Read an entry. The entries we read can be cached in case we need them
     * later.
     */
    private void readFile(int i) {
	timestamp = inputs[i].getTime();
	try {
	    if (cached) {
		if (!ppmap.containsKey(i)) {
		    ppmap.put(i, new JSONParser(zf.getInputStream(inputs[i])));
		}
		kstats = ppmap.get(i).getKstats();
	    } else {
		JSONParser jp = new JSONParser(zf.getInputStream(inputs[i]));
		kstats = jp.getKstats();
	    }
	} catch (IOException ioe) {
	    kstats.clear();
	}
    }
}
