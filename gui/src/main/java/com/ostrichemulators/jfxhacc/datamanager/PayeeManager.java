/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Payee;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PayeeManager extends NamedIDableDataManager<Payee> {

	private static final Logger log = Logger.getLogger( PayeeManager.class );

	public PayeeManager( DataEngine de ) {
		super( de.getPayeeMapper() );
	}
}
