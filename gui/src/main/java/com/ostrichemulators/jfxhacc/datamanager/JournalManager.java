/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Journal;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class JournalManager extends NamedIDableDataManager<Journal> {

	private static final Logger log = Logger.getLogger( JournalManager.class );

	public JournalManager( DataEngine de ) {
		super( de.getJournalMapper() );
	}
}
