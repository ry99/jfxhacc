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

	public static final String PREFIX = "jfxhacc";
	public static final String BASE = "http://com.ostrich-emulators/jfxhacc";
	public static final String NAMESPACE = BASE + "/";

	public static final URI DATASET_TYPE = new URIImpl( NAMESPACE + "dataset" );
	public static final URI DATASET_VERSION = new URIImpl( NAMESPACE + "version" );
	public static final URI MAJOR_VERSION = new URIImpl( NAMESPACE + "major-version" );
	public static final URI MINOR_VERSION = new URIImpl( NAMESPACE + "minor-version" );
	public static final URI REVISION_VERSION = new URIImpl( NAMESPACE + "revision-version" );

	public static final int MAJORV = 1;
	public static final int MINORV = 0;
	public static final int REVV = 0;

	public static final URI ASSET = new URIImpl( NAMESPACE + "asset" );
	public static final URI EQUITY = new URIImpl( NAMESPACE + "equity" );
	public static final URI LIABILITY = new URIImpl( NAMESPACE + "liability" );
	public static final URI REVENUE = new URIImpl( NAMESPACE + "revenue" );
	public static final URI EXPENSE = new URIImpl( NAMESPACE + "expense" );
}
