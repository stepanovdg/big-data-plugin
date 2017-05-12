package org.pentaho.big.data.impl.vfs.hdfs;

import org.apache.commons.vfs2.FileSystem;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;

/**
 * Created by dstepanov on 30/05/17.
 */
public class HDFSConfigBuilder extends KettleGenericFileSystemConfigBuilder {

  private static final HDFSConfigBuilder BUILDER = new HDFSConfigBuilder();

  /**
   * @return NamedClusterConfigBuilder instance
   */
  public static HDFSConfigBuilder getInstance() {
    return BUILDER;
  }

  /**
   * @return HDFSFileSystem
   */
  @Override
  protected Class<? extends FileSystem> getConfigClass() {
    return HDFSFileSystem.class;
  }

}
