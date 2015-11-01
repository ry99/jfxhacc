/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import java.util.Map;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface Transaction extends IDable {

	public Date getDate();

	public void setDate( Date date );

	public void setPayee( Payee payee );

	public Payee getPayee();

	public Map<Split, Account> getSplits();

	public void setSplits( Map<Split, Account> splits );

	public void addSplit( Split s, Account a );
}
