/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.view;

import com.jme3.scene.Spatial;
import com.simsilica.es.Entity;

/**
 *
 * @author Asser
 */
public interface ModelFactory {
    
    public void setState( ModelViewState state );
    public Spatial createModel( Entity e );
}
