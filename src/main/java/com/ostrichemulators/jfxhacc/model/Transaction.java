/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author ryan
 */
public interface Transaction extends IDable {

	public Set<Split> getSplits();

	public void setSplits( Set<Split> splits );

	public void addSplit( Split s );

	public Date getDate();

	public void setDate( Date date );

	public boolean isBalanced();

	public void setPayee( String payee );

	public String getPayee();

}
