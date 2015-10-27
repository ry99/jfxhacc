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
public class JfxHacc {

	private JfxHacc() {
	}

	public static final String PREFIX = "jhacc";
	public static final String BASE = "http://com.ostrich-emulators/jfxhacc";
	public static final String NAMESPACE = BASE + "/";

	public static final URI DATASET_TYPE = new URIImpl( NAMESPACE + "dataset" );
	//public static final URI VOID_TYPE = new URIImpl( "http://rdfs.org/ns/void#Dataset" );
	
	public static final URI ACCOUNT_TYPE = new URIImpl( NAMESPACE + "account" );
	public static final URI TRANSACTION_TYPE = new URIImpl( NAMESPACE + "transaction" );
	public static final URI JOURNAL_TYPE = new URIImpl( NAMESPACE + "journal" );
	public static final URI SPLIT_TYPE = new URIImpl( NAMESPACE + "split" );

	public static final URI ASSET = new URIImpl( NAMESPACE + "asset" );
	public static final URI EQUITY = new URIImpl( NAMESPACE + "equity" );
	public static final URI LIABILITY = new URIImpl( NAMESPACE + "liability" );
	public static final URI REVENUE = new URIImpl( NAMESPACE + "revenue" );
	public static final URI EXPENSE = new URIImpl( NAMESPACE + "expense" );

}
