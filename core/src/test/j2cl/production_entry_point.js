goog.module('zemeckis.j2cl.ProductionEntryPoint');

const BuildTest = goog.require('zemeckis.j2cl.BuildTest');

goog.exportSymbol('zemeckis.j2cl.ProductionBuildTest', BuildTest);
goog.exportProperty(BuildTest, 'scheduleProductionTasks', BuildTest.scheduleProductionTasks);
