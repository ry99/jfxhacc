/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.IDable;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class IDableImpl implements IDable {

	private URI id;
	private final URI type;

	protected IDableImpl( URI type, URI id ) {
		this.id = id;
		this.type = type;
	}

	protected IDableImpl( URI type ) {
		this( type, UriUtil.randomUri( type ) );
	}

	@Override
	public URI getId() {
		return id;
	}

	@Override
	public void setId( URI id ) {
		this.id = id;
	}

	@Override
	public URI getType() {
		return type;
	}
}
