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
public class Splits {

	private Splits() {
	}

	public static final String PREFIX = "splits";
	public static final String BASE = "http://com.ostrich-emulators/jfxhacc/split";
	public static final String NAMESPACE = BASE + "/";

	public static final URI ACCOUNT_PRED = new URIImpl( NAMESPACE + "account" );
	public static final URI VALUE_PRED = new URIImpl( NAMESPACE + "value" );
	public static final URI RECO_PRED = new URIImpl( NAMESPACE + "reconciled" );
	public static final URI MEMO_PRED = new URIImpl( NAMESPACE + "memo" );
}
