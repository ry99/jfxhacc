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
public class Loans {

	private Loans() {
	}

	public static final String PREFIX = "loans";
	public static final String BASE = JfxHacc.BASE + "/loan";
	public static final String NAMESPACE = BASE + "/";

	public static final URI TYPE = new URIImpl( BASE );

	public static final URI PCT_PRED = new URIImpl( NAMESPACE + "apr" );
	public static final URI NUMPAYMENTS_PRED = new URIImpl( NAMESPACE + "numberOfPayments" );
	public static final URI VALUE_PRED = new URIImpl( NAMESPACE + "amount" );
}
