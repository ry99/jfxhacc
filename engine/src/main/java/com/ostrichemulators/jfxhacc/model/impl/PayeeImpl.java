/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.vocabulary.Payees;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class PayeeImpl extends NamedIDableImpl implements Payee {

	public PayeeImpl( URI id, String name ) {
		super( Payees.TYPE, id, name );
	}
}
