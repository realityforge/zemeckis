goog.module('zemeckis.j2cl.EntryPoint');

const BuildTest = goog.require('zemeckis.j2cl.BuildTest');
const Zemeckis = goog.require('zemeckis.Zemeckis');
const ZemeckisConfig = goog.require('zemeckis.ZemeckisConfig');
const ZemeckisTestUtil = goog.require('zemeckis.ZemeckisTestUtil');

goog.exportSymbol('zemeckis.j2cl.BuildTest', BuildTest);
goog.exportProperty(BuildTest, 'pumpAll', BuildTest.pumpAll);
goog.exportProperty(BuildTest, 'scheduleAndPump', BuildTest.scheduleAndPump);
goog.exportProperty(BuildTest, 'scheduleTask', BuildTest.scheduleTask);
goog.exportSymbol('zemeckis.j2cl.Zemeckis', Zemeckis);
goog.exportSymbol('zemeckis.j2cl.ZemeckisConfig', ZemeckisConfig);
goog.exportSymbol('zemeckis.j2cl.ZemeckisTestUtil', ZemeckisTestUtil);
