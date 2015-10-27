/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import java.util.UUID;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class UriUtil {

	private UriUtil() {
	}

	public static URI randomUri( URI type ) {
		String ns = type.stringValue();
		String newname = UUID.randomUUID().toString();
		if ( !( ns.endsWith( "#" ) || ns.endsWith( "/" ) ) ) {
			newname = "#" + newname;
		}

		return new URIImpl( ns + newname );
	}
}
