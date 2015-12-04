/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.utility.TreeNode;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ryan
 */
public interface AccountMapper extends DataMapper<Account> {

	public static enum BalanceType {

		OPENING, CURRENT, RECONCILED
	};

	public Money getBalance( Account a, BalanceType type );

	public Money getBalance( Account a, BalanceType type, Date asOf );

	public Account getParent( Account a ) throws MapperException;

	public Account create( String name, AccountType type, Money obal, String notes,
			String number, Account parent ) throws MapperException;

	public void update( Account acct, Account parent ) throws MapperException;

	public TreeNode<Account> getAccounts( AccountType type ) throws MapperException;

	public List<Account> getParents( Account a ) throws MapperException;

	/**
	 * Gets a mapping of account-to-parent
	 *
	 * @return a mapping of accounts to their parents
	 * @throws MapperException
	 */
	public Map<Account, Account> getParentMap() throws MapperException;

	/**
	 * Gets an ordered (high to low) list of popular accounts for this payee,
	 * excluding the given account
	 *
	 * @param p
	 * @param except don't include this account in calculations
	 * @return
	 * @throws MapperException
	 */
	public List<Account> getPopularAccounts( Payee p, Account except ) throws MapperException;
}
