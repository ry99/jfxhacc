/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import java.util.Map;

/**
 * A transaction is a complex class that brings together splits, accounts, and
 * payees
 *
 * @author ryan
 */
public interface Transaction extends IDable, Comparable<Transaction> {

	public Date getDate();

	public void setDate( Date date );

	public void setPayee( Payee payee );

	public Payee getPayee();

	public Map<Account, Split> getSplits();

	public void setSplits( Map<Account, Split> splits );

	public void addSplit( Account a, Split s );

	public String getNumber();

	public void setNumber( String s );
}
