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
public class Accounts {

	private Accounts() {
	}

	public static final String PREFIX = "accounts";
	public static final String BASE = "http://com.ostrich-emulators/jfxhacc/accounts";
	public static final String NAMESPACE = BASE + "/";

	public static final URI TYPE_PRED = new URIImpl( NAMESPACE + "accountType" );
	public static final URI OBAL_PRED = new URIImpl( NAMESPACE + "openingBalance" );
	public static final URI PARENT_PRED = new URIImpl( NAMESPACE + "parent" );
	public static final URI NUMBER_PRED = new URIImpl( NAMESPACE + "number" );
	public static final URI NOTES_PRED = new URIImpl( NAMESPACE + "notes" );
}
