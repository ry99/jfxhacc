/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.Property;

/**
 *
 * @author ryan
 */
public interface Split extends SplitBase {

	public void setAccount( Account a );

	public Account getAccount();

	public Property<Account> getAccountProperty();
}
