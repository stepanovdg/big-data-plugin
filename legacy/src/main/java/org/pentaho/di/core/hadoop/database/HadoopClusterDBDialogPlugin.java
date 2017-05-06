package org.pentaho.di.core.hadoop.database;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.util.ResourceBundle;

/**
 * Created by dstepanov on 06/05/17.
 */

@SpoonPluginCategories( { "connection_dialog" } )
@SpoonPlugin( id = "HadoopClusterDBDialogPlugin", image = "" )
public class HadoopClusterDBDialogPlugin implements SpoonPluginInterface {
  private static Class<?> PKG = HadoopClusterDBDialogPlugin.class;

  private LogChannelInterface log = new LogChannel( HadoopClusterDBDialogPlugin.class.getName() );

  private ResourceBundle resourceBundle = new XulSpoonResourceBundle( PKG );

  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    container.registerClassLoader( getClass().getClassLoader() );
    container.addEventHandler( new HadoopDataHandler() );
    container.addEventHandler( new HadoopFragmentHandler() );

  }

  @Override public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  @Override public SpoonPerspective getPerspective() {
    return null;
  }
}
