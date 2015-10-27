/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

/**
 *
 * @author ryan
 */
public interface Account extends IDable {

	public String getName();

	public void setName( String n );

	public AccountType getAccountType();

	public void setOpeningBalance( Money money );

	public Money getOpeningBalance();
}
