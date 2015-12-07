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
public class Journals {

	private Journals() {
	}

	public static final String PREFIX = "journals";
	public static final String BASE = JfxHacc.BASE + "/journal";
	public static final String NAMESPACE = BASE + "/";

	public static final URI TYPE = new URIImpl( BASE );
}
