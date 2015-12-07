/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class Recurrings {

	private Recurrings() {
	}

	public static final String PREFIX = "recur";
	public static final String BASE = "http://com.ostrich-emulators/jfxhacc/recurring";
	public static final String NAMESPACE = BASE + "/";

	public static final URI NEXTRUN_PRED = new URIImpl( NAMESPACE + "nextrun" );
	public static final URI FREQUENCY_PRED = new URIImpl( NAMESPACE + "freq" );
}
