package org.pentaho.di.core.hadoop.database;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.database.dialog.DataOverrideHandler;
import org.pentaho.ui.xul.components.XulMenuList;

/**
 * Created by dstepanov on 06/05/17.
 */
public class HadoopDataHandler extends DataOverrideHandler {

  // Pentaho bigdata services specific
  private XulMenuList namedCLusterName;

  @Override public String getName() {
    return "dataHandler";
  }

  @Override protected void getConnectionSpecificInfo( DatabaseMeta meta ) {
    super.getConnectionSpecificInfo( meta );
    if ( namedCLusterName != null ) {
      meta.getAttributes().put( "pentahoNamedCluster", namedCLusterName.getSelectedItem() );
    }
  }

  @Override protected void getControls() {
    super.getControls();
    namedCLusterName = (XulMenuList) document.getElementById( "namedcluster-menu-list" );
  }

  @Override protected void setConnectionSpecificInfo( DatabaseMeta meta ) {
    super.setConnectionSpecificInfo( meta );

    if ( namedCLusterName != null ) {
      Object value = meta.getAttributes().get( "pentahoNamedCluster" );
      if ( value != null && value instanceof String ) {
          namedCLusterName.setSelectedItem( value );
      } else {
          namedCLusterName.setSelectedItem( null );
      }
    }
  }


}
