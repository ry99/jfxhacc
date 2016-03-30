/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class AccountHelper {

	public static final Logger log = Logger.getLogger( AccountHelper.class );

	private AccountHelper() {
	}

	public static boolean increasesAccountValue( Split s, Account a ) {
		return ( s.isDebit() == a.getAccountType().isDebitPlus() );
	}

	public static Money getSplitValueForAccount( Split s, Account a ) {
		Money m = s.getValue(); // split.getValue() is always positive
		return ( increasesAccountValue( s, a ) ? m : m.opposite() );
	}
}
