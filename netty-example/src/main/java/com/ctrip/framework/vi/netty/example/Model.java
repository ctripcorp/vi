package com.ctrip.framework.cornerstone.netty.example;

/**
 * Created by jiang.j on 2017/2/21.
 */
abstract interface Model
{
  public abstract long getNewGenMinSize();

  public abstract long getNewGenMaxSize();

  public abstract long getNewGenCurSize();

  public abstract long getEdenUsed();

  public abstract long getSurvivor0Used();

  public abstract long getSurvivor1Used();

  public abstract long getTenuredUsed();

  public abstract long getPermUsed();

  public abstract long getEdenSize();

  public abstract long getSurvivor0Size();

  public abstract long getSurvivor1Size();

  public abstract long getTenuredSize();

  public abstract long getPermSize();

  public abstract long getEdenCapacity();

  public abstract long getSurvivor0Capacity();

  public abstract long getSurvivor1Capacity();

  public abstract long getTenuredCapacity();

  public abstract long getPermCapacity();

  public abstract long getEdenGCEvents();

  public abstract long getTenuredGCEvents();

  public abstract long getEdenGCTime();

  public abstract long getTenuredGCTime();

  public abstract long getTenuringThreshold();

  public abstract long getMaxTenuringThreshold();

  public abstract long getDesiredSurvivorSize();

  public abstract long getAgeTableSize();

  public abstract String getLastGCCause();

  public abstract String getCurrentGCCause();

  public abstract long[] getAgeTableSizes();

  public abstract void getAgeTableSizes(long[] paramArrayOfLong);

  public abstract long getClassLoadTime();

  public abstract long getClassesLoaded();

  public abstract long getClassesUnloaded();

  public abstract long getClassBytesLoaded();

  public abstract long getClassBytesUnloaded();

  public abstract long getTotalCompileTime();

  public abstract long getTotalCompile();

  public abstract void initializeFinalizer();

  public abstract boolean isFinalizerInitialized();

  public abstract long getFinalizerTime();

  public abstract long getFinalizerCount();

  public abstract long getFinalizerQLength();

  public abstract long getFinalizerQMaxLength();

  public abstract long getOsElapsedTime();

  public abstract long getOsFrequency();

  public abstract String getJavaCommand();

  public abstract String getJavaHome();

  public abstract String getVmArgs();

  public abstract String getVmFlags();

  public abstract String getClassPath();

  public abstract String getEndorsedDirs();

  public abstract String getExtDirs();

  public abstract String getLibraryPath();

  public abstract String getBootClassPath();

  public abstract String getBootLibraryPath();

  public abstract String getVmInfo();

  public abstract String getVmName();

  public abstract String getVmVersion();

  public abstract String getVmVendor();

  public abstract String getVmSpecName();

  public abstract String getVmSpecVersion();

  public abstract String getVmSpecVendor();

  public abstract long getLastModificationTime();
}
