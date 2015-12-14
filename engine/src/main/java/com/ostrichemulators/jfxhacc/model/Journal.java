/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.StringProperty;

/**
 *
 * @author ryan
 */
public interface Journal extends IDable {

	public String getName();

	public void setName( String name );

	public StringProperty getNameProperty();
}
