/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class GuiUtils {

	public static final Logger log = Logger.getLogger( GuiUtils.class );

	private GuiUtils() {
	}

	public static String getFullName( Account a, AccountMapper amap ) {
		try {
			List<Account> parents = amap.getParents( a );
			StringBuilder sb = new StringBuilder();
			for ( Account parent : parents ) {
				sb.append( parent.getName() ).append( "::" );
			}
			sb.append( a.getName() );
			return sb.toString();
		}
		catch ( MapperException me ) {
			log.warn( me, me );
		}

		return a.getName();
	}
}
