/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model.impl;

import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.vocabulary.Journals;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class JournalImpl extends NamedIDableImpl implements Journal {

	public JournalImpl(URI id, String name ) {
		super( Journals.TYPE, id, name );
	}
}
