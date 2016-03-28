/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;

/**
 *
 * @author ryan
 */
public class Transactions {

	private Transactions() {
	}

	public static final String PREFIX = "trans";
	public static final String BASE = JfxHacc.BASE + "/transaction";
	public static final String NAMESPACE = BASE + "/";

	public static final URI TYPE = new URIImpl( BASE );

	public static final URI DATE_PRED = DCTERMS.CREATED;
	public static final URI PAYEE_PRED = new URIImpl( NAMESPACE + "payee" );
	public static final URI SPLIT_PRED = new URIImpl( NAMESPACE + "entry" );
	public static final URI NUMBER_PRED = new URIImpl( NAMESPACE + "number" );
	public static final URI JOURNAL_PRED = new URIImpl( NAMESPACE + "journal" );

}
