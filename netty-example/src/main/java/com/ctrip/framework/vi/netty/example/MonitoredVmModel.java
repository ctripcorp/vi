package com.ctrip.framework.vi.netty.example;

/**
 * Created by jiang.j on 2017/2/21.
 */


import sun.jvmstat.monitor.LongMonitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.StringMonitor;

class MonitoredVmModel
  implements Model
{
  private MonitoredVm vm;
  private LongMonitor newGenMinSize;
  private LongMonitor newGenMaxSize;
  private LongMonitor newGenCurSize;
  private LongMonitor edenSize;
  private LongMonitor edenCapacity;
  private LongMonitor edenUsed;
  private LongMonitor edenGCTime;
  private LongMonitor edenGCEvents;
  private LongMonitor survivor0Size;
  private LongMonitor survivor0Capacity;
  private LongMonitor survivor0Used;
  private LongMonitor survivor1Size;
  private LongMonitor survivor1Capacity;
  private LongMonitor survivor1Used;
  private LongMonitor tenuredSize;
  private LongMonitor tenuredCapacity;
  private LongMonitor tenuredUsed;
  private LongMonitor tenuredGCTime;
  private LongMonitor tenuredGCEvents;
  private LongMonitor permSize;
  private LongMonitor permCapacity;
  private LongMonitor permUsed;
  private LongMonitor tenuringThreshold;
  private LongMonitor maxTenuringThreshold;
  private LongMonitor desiredSurvivorSize;
  private LongMonitor ageTableSize;
  private LongMonitor[] ageTableSizes;
  private StringMonitor lastGCCause;
  private StringMonitor currentGCCause;
  private StringMonitor collector0name;
  private StringMonitor collector1name;
  private boolean finalizerInitialized = false;
  private LongMonitor finalizerTime;
  private LongMonitor finalizerQLength;
  private LongMonitor finalizerQMaxLength;
  private LongMonitor finalizerCount;
  private LongMonitor classLoadTime;
  private LongMonitor classesLoaded;
  private LongMonitor classesUnloaded;
  private LongMonitor classBytesLoaded;
  private LongMonitor classBytesUnloaded;
  private LongMonitor totalCompileTime;
  private LongMonitor totalCompile;
  private LongMonitor osElapsedTime;
  private LongMonitor osFrequency;
  private StringMonitor javaCommand;
  private StringMonitor javaHome;
  private StringMonitor vmArgs;
  private StringMonitor vmFlags;
  private StringMonitor vmInfo;
  private StringMonitor vmName;
  private StringMonitor vmVersion;
  private StringMonitor vmVendor;
  private StringMonitor vmSpecName;
  private StringMonitor vmSpecVersion;
  private StringMonitor vmSpecVendor;
  private StringMonitor classPath;
  private StringMonitor bootClassPath;
  private StringMonitor libraryPath;
  private StringMonitor bootLibraryPath;
  private StringMonitor endorsedDirs;
  private StringMonitor extDirs;
  private LongMonitor lastModificationTime;

  synchronized void initialize_finalizer()
  {
    if (this.finalizerInitialized) {
      return;
    }
    try
    {
      this.finalizerQLength = ((LongMonitor)this.vm.findByName("sun.gc.finalizer.queue.length"));
      this.finalizerQMaxLength = ((LongMonitor)this.vm.findByName("sun.gc.finalizer.queue.maxLength"));
      this.finalizerTime = ((LongMonitor)this.vm.findByName("sun.gc.finalizer.time"));
      this.finalizerCount = ((LongMonitor)this.vm.findByName("sun.gc.finalizer.objects"));
      if (this.finalizerQLength == null) {
        return;
      }
      this.finalizerInitialized = true;
    }
    catch (MonitorException localMonitorException) {}
  }

  private void initialize_post_1_4_1()
    throws MonitorException
  {
    this.newGenMaxSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.maxCapacity"));
    this.newGenMinSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.minCapacity"));
    this.newGenCurSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.capacity"));
    this.lastGCCause = ((StringMonitor)this.vm.findByName("sun.gc.lastCause"));
    this.currentGCCause = ((StringMonitor)this.vm.findByName("sun.gc.cause"));
    this.collector0name = ((StringMonitor)this.vm.findByName("sun.gc.collector.0.name"));
  }

  private void initialize_common()
    throws MonitorException
  {
    this.survivor0Size = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.1.maxCapacity"));
    this.survivor0Capacity = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.1.capacity"));
    this.survivor0Used = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.1.used"));

    this.survivor1Size = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.2.maxCapacity"));
    this.survivor1Capacity = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.2.capacity"));
    this.survivor1Used = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.2.used"));

    this.edenSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.0.maxCapacity"));
    this.edenCapacity = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.0.capacity"));
    this.edenUsed = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.space.0.used"));

    this.tenuredSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.1.space.0.maxCapacity"));
    this.tenuredCapacity = ((LongMonitor)this.vm.findByName("sun.gc.generation.1.space.0.capacity"));
    this.tenuredUsed = ((LongMonitor)this.vm.findByName("sun.gc.generation.1.space.0.used"));

    this.permSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.2.space.0.maxCapacity"));
    this.permCapacity = ((LongMonitor)this.vm.findByName("sun.gc.generation.2.space.0.capacity"));
    this.permUsed = ((LongMonitor)this.vm.findByName("sun.gc.generation.2.space.0.used"));

    this.edenGCEvents = ((LongMonitor)this.vm.findByName("sun.gc.collector.0.invocations"));
    this.edenGCTime = ((LongMonitor)this.vm.findByName("sun.gc.collector.0.time"));

    this.tenuredGCEvents = ((LongMonitor)this.vm.findByName("sun.gc.collector.1.invocations"));
    this.tenuredGCTime = ((LongMonitor)this.vm.findByName("sun.gc.collector.1.time"));

    this.ageTableSize = ((LongMonitor)this.vm.findByName("sun.gc.generation.0.agetable.size"));
    if (this.ageTableSize != null)
    {
      this.maxTenuringThreshold = ((LongMonitor)this.vm.findByName("sun.gc.policy.maxTenuringThreshold"));
      this.tenuringThreshold = ((LongMonitor)this.vm.findByName("sun.gc.policy.tenuringThreshold"));
      this.desiredSurvivorSize = ((LongMonitor)this.vm.findByName("sun.gc.policy.desiredSurvivorSize"));
      int i = (int)this.ageTableSize.longValue();
      this.ageTableSizes = new LongMonitor[i];

      String str = "sun.gc.generation.0.agetable.bytes.";
      for (int j = 0; j < i; j++) {
        if (j < 10) {
          this.ageTableSizes[j] = ((LongMonitor)this.vm.findByName(str + "0" + j));
        } else {
          this.ageTableSizes[j] = ((LongMonitor)this.vm.findByName(str + j));
        }
      }
    }
    this.classLoadTime = ((LongMonitor)this.vm.findByName("sun.cls.time"));
    this.classesLoaded = ((LongMonitor)this.vm.findByName("java.cls.loadedClasses"));
    this.classesUnloaded = ((LongMonitor)this.vm.findByName("java.cls.unloadedClasses"));
    this.classBytesLoaded = ((LongMonitor)this.vm.findByName("sun.cls.loadedBytes"));
    this.classBytesUnloaded = ((LongMonitor)this.vm.findByName("sun.cls.unloadedBytes"));

    this.totalCompileTime = ((LongMonitor)this.vm.findByName("java.ci.totalTime"));
    this.totalCompile = ((LongMonitor)this.vm.findByName("sun.ci.totalCompiles"));
    try
    {
      initialize_finalizer();
    }
    catch (Throwable localThrowable) {}

    this.osElapsedTime = ((LongMonitor)this.vm.findByName("sun.os.hrt.ticks"));
    this.osFrequency = ((LongMonitor)this.vm.findByName("sun.os.hrt.frequency"));
    this.javaCommand = ((StringMonitor)this.vm.findByName("sun.rt.javaCommand"));

    this.javaHome = ((StringMonitor)this.vm.findByName("java.property.java.home"));
    this.vmArgs = ((StringMonitor)this.vm.findByName("java.rt.vmArgs"));
    this.vmFlags = ((StringMonitor)this.vm.findByName("java.rt.vmFlags"));
    this.vmInfo = ((StringMonitor)this.vm.findByName("java.property.java.vm.info"));
    this.vmName = ((StringMonitor)this.vm.findByName("java.property.java.vm.name"));
    this.vmVersion = ((StringMonitor)this.vm.findByName("java.property.java.vm.version"));
    this.vmVendor = ((StringMonitor)this.vm.findByName("java.property.java.vm.vendor"));
    this.vmSpecName = ((StringMonitor)this.vm.findByName("java.property.java.vm.specification.name"));
    this.vmSpecVersion = ((StringMonitor)this.vm.findByName("java.property.java.vm.specification.version"));
    this.vmSpecVendor = ((StringMonitor)this.vm.findByName("java.property.java.vm.specification.vendor"));
    this.classPath = ((StringMonitor)this.vm.findByName("java.property.java.class.path"));
    this.bootClassPath = ((StringMonitor)this.vm.findByName("sun.property.sun.boot.class.path"));
    this.libraryPath = ((StringMonitor)this.vm.findByName("java.property.java.library.path"));
    this.bootLibraryPath = ((StringMonitor)this.vm.findByName("sun.property.sun.boot.library.path"));
    this.endorsedDirs = ((StringMonitor)this.vm.findByName("java.property.java.endorsed.dirs"));
    this.extDirs = ((StringMonitor)this.vm.findByName("java.property.java.ext.dirs"));

    this.lastModificationTime = ((LongMonitor)this.vm.findByName("sun.perfdata.timestamp"));
  }

  void initialize()
    throws MonitorException
  {
    initialize_common();
    if (!this.vmVersion.stringValue().startsWith("1.4.1")) {
      initialize_post_1_4_1();
    }
  }

  public MonitoredVmModel(MonitoredVm paramMonitoredVm)
    throws MonitorException
  {
    this.vm = paramMonitoredVm;
    initialize();
  }

  public long getNewGenMaxSize()
  {
    if (this.newGenMaxSize != null) {
      return this.newGenMaxSize.longValue();
    }
    return this.edenSize.longValue() + this.survivor0Size.longValue() + this.survivor1Size.longValue();
  }

  public long getNewGenMinSize()
  {
    if (this.newGenMinSize != null) {
      return this.newGenMinSize.longValue();
    }
    return this.edenSize.longValue() + this.survivor0Size.longValue() + this.survivor1Size.longValue();
  }

  public long getNewGenCurSize()
  {
    if (this.newGenCurSize != null) {
      return this.newGenCurSize.longValue();
    }
    return this.edenSize.longValue() + this.survivor0Size.longValue() + this.survivor1Size.longValue();
  }

  public String getLastGCCause()
  {
    if (this.lastGCCause == null) {
      return null;
    }
    return this.lastGCCause.stringValue();
  }

  public String getCurrentGCCause()
  {
    if (this.currentGCCause == null) {
      return null;
    }
    return this.currentGCCause.stringValue();
  }

  public long getEdenUsed()
  {
    return this.edenUsed.longValue();
  }

  public long getTenuredUsed()
  {
    return this.tenuredUsed.longValue();
  }

  public long getSurvivor0Used()
  {
    return this.survivor0Used.longValue();
  }

  public long getSurvivor1Used()
  {
    return this.survivor1Used.longValue();
  }

  public long getPermUsed()
  {
    return this.permUsed.longValue();
  }

  public long getEdenSize()
  {
    return this.edenSize.longValue();
  }

  public long getSurvivor0Size()
  {
    return this.survivor0Size.longValue();
  }

  public long getSurvivor1Size()
  {
    return this.survivor1Size.longValue();
  }

  public long getTenuredSize()
  {
    return this.tenuredSize.longValue();
  }

  public long getPermSize()
  {
    return this.permSize.longValue();
  }

  public long getEdenCapacity()
  {
    return this.edenCapacity.longValue();
  }

  public long getSurvivor0Capacity()
  {
    return this.survivor0Capacity.longValue();
  }

  public long getSurvivor1Capacity()
  {
    return this.survivor1Capacity.longValue();
  }

  public long getTenuredCapacity()
  {
    return this.tenuredCapacity.longValue();
  }

  public long getPermCapacity()
  {
    return this.permCapacity.longValue();
  }

  public long getEdenGCEvents()
  {
    return this.edenGCEvents.longValue();
  }

  public long getTenuredGCEvents()
  {
    return this.tenuredGCEvents.longValue();
  }

  public long getEdenGCTime()
  {
    return this.edenGCTime.longValue();
  }

  public long getTenuredGCTime()
  {
    return this.tenuredGCTime.longValue();
  }

  public long getTenuringThreshold()
  {
    if (this.tenuringThreshold == null) {
      return 0L;
    }
    return this.tenuringThreshold.longValue();
  }

  public long getMaxTenuringThreshold()
  {
    if (this.maxTenuringThreshold == null) {
      return 0L;
    }
    return this.maxTenuringThreshold.longValue();
  }

  public long getDesiredSurvivorSize()
  {
    if (this.desiredSurvivorSize == null) {
      return 0L;
    }
    return this.desiredSurvivorSize.longValue();
  }

  public long getAgeTableSize()
  {
    if (this.ageTableSize == null) {
      return 0L;
    }
    return this.ageTableSize.longValue();
  }

  public long[] getAgeTableSizes()
  {
    if (this.ageTableSize == null) {
      return null;
    }
    long[] arrayOfLong = new long[this.ageTableSizes.length];
    for (int i = 0; i < this.ageTableSizes.length; i++) {
      arrayOfLong[i] = this.ageTableSizes[i].longValue();
    }
    return arrayOfLong;
  }

  public void getAgeTableSizes(long[] paramArrayOfLong)
  {
    if (this.ageTableSize == null) {
      return;
    }
    for (int i = 0; i < this.ageTableSizes.length; i++) {
      paramArrayOfLong[i] = this.ageTableSizes[i].longValue();
    }
  }

  public long getClassLoadTime()
  {
    return this.classLoadTime.longValue();
  }

  public long getClassesLoaded()
  {
    return this.classesLoaded.longValue();
  }

  public long getClassesUnloaded()
  {
    return this.classesUnloaded.longValue();
  }

  public long getClassBytesLoaded()
  {
    return this.classBytesLoaded.longValue();
  }

  public long getClassBytesUnloaded()
  {
    return this.classBytesUnloaded.longValue();
  }

  public long getTotalCompileTime()
  {
    return this.totalCompileTime.longValue();
  }

  public long getTotalCompile()
  {
    return this.totalCompile.longValue();
  }

  public boolean isFinalizerInitialized()
  {
    return this.finalizerInitialized;
  }

  public void initializeFinalizer()
  {
    initialize_finalizer();
  }

  public long getFinalizerTime()
  {
    synchronized (this)
    {
      if ((this.finalizerInitialized) && (this.finalizerTime == null)) {
        try
        {
          this.finalizerTime = ((LongMonitor)this.vm.findByName("sun.gc.finalizer.time"));
        }
        catch (MonitorException localMonitorException) {}
      }
      if (this.finalizerTime == null) {
        return 0L;
      }
      return this.finalizerTime.longValue();
    }
  }

  public long getFinalizerCount()
  {
    synchronized (this)
    {
      if ((this.finalizerInitialized) && (this.finalizerCount == null)) {
        try
        {
          this.finalizerCount = ((LongMonitor)this.vm.findByName("sun.gc.finalizer.objects"));
        }
        catch (MonitorException localMonitorException) {}
      }
      if (this.finalizerCount == null) {
        return 0L;
      }
      return this.finalizerCount.longValue();
    }
  }

  public long getFinalizerQLength()
  {
    if (this.finalizerQLength == null) {
      return 0L;
    }
    return this.finalizerQLength.longValue();
  }

  public long getFinalizerQMaxLength()
  {
    if (this.finalizerQMaxLength == null) {
      return 0L;
    }
    return this.finalizerQMaxLength.longValue();
  }

  public long getOsElapsedTime()
  {
    return this.osElapsedTime.longValue();
  }

  public long getOsFrequency()
  {
    return this.osFrequency.longValue();
  }

  public String getJavaCommand()
  {
    if (this.javaCommand == null) {
      return null;
    }
    return this.javaCommand.stringValue();
  }

  public String getJavaHome()
  {
    if (this.javaHome == null) {
      return null;
    }
    return this.javaHome.stringValue();
  }

  public String getVmArgs()
  {
    if (this.vmArgs == null) {
      return null;
    }
    return this.vmArgs.stringValue();
  }

  public String getVmFlags()
  {
    if (this.vmFlags == null) {
      return null;
    }
    return this.vmFlags.stringValue();
  }

  public String getClassPath()
  {
    if (this.classPath == null) {
      return null;
    }
    return this.classPath.stringValue();
  }

  public String getEndorsedDirs()
  {
    if (this.endorsedDirs == null) {
      return null;
    }
    return this.endorsedDirs.stringValue();
  }

  public String getExtDirs()
  {
    if (this.extDirs == null) {
      return null;
    }
    return this.extDirs.stringValue();
  }

  public String getLibraryPath()
  {
    if (this.libraryPath == null) {
      return null;
    }
    return this.libraryPath.stringValue();
  }

  public String getBootClassPath()
  {
    if (this.bootClassPath == null) {
      return null;
    }
    return this.bootClassPath.stringValue();
  }

  public String getBootLibraryPath()
  {
    if (this.bootLibraryPath == null) {
      return null;
    }
    return this.bootLibraryPath.stringValue();
  }

  public String getVmInfo()
  {
    if (this.vmInfo == null) {
      return null;
    }
    return this.vmInfo.stringValue();
  }

  public String getVmName()
  {
    if (this.vmName == null) {
      return null;
    }
    return this.vmName.stringValue();
  }

  public String getVmVersion()
  {
    if (this.vmVersion == null) {
      return null;
    }
    return this.vmVersion.stringValue();
  }

  public String getVmVendor()
  {
    if (this.vmVendor == null) {
      return null;
    }
    return this.vmVendor.stringValue();
  }

  public String getVmSpecName()
  {
    if (this.vmSpecName == null) {
      return null;
    }
    return this.vmSpecName.stringValue();
  }

  public String getVmSpecVersion()
  {
    if (this.vmSpecVersion == null) {
      return null;
    }
    return this.vmSpecVersion.stringValue();
  }

  public String getVmSpecVendor()
  {
    if (this.vmSpecVendor == null) {
      return null;
    }
    return this.vmSpecVendor.stringValue();
  }

  public long getLastModificationTime()
  {
    return this.lastModificationTime.longValue();
  }
}
