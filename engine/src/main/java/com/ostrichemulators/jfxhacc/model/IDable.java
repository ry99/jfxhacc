/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.model;

import javafx.beans.property.Property;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface IDable {

	public void setId( URI id );

	public URI getId();

	public Property<URI> getIdProperty();

	public URI getType();
}
