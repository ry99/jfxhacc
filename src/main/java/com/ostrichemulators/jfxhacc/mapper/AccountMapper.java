/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;

/**
 *
 * @author ryan
 */
public interface AccountMapper extends DataMapper<Account> {

	public static enum BalanceType {

		OPENING, CURRENT, RECONCILED
	};

	public Money getBalance( Account a, BalanceType type );
}
