/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.IDable;
import javafx.beans.property.StringProperty;

/**
 * An IDable with a String name (as of now, {@link Journal} and {@link Payee})
 *
 * @author ryan
 */
public interface NamedIDable extends IDable {

	public String getName();

	public StringProperty getNameProperty();

	public void setName( String name );

}
