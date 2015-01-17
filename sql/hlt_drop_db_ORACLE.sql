--
-- drop all tables and sequences
--

DROP TABLE    SuperIdReleaseAssoc;
DROP TABLE    ConfigurationPathAssoc;
DROP TABLE    PathStreamDatasetAssoc;
DROP TABLE    StreamDatasetAssoc;
DROP TABLE    PathInPathAssoc;
DROP TABLE    PathModuleAssoc;
DROP TABLE    PathOutputModAssoc;
DROP TABLE    ConfigurationSequenceAssoc;
DROP TABLE    PathSequenceAssoc;
DROP TABLE    SequenceInSequenceAssoc;
DROP TABLE    SequenceModuleAssoc;
DROP TABLE    SequenceOutputModAssoc;
DROP TABLE    ConfigurationServiceAssoc;
DROP TABLE    ConfigurationEDSourceAssoc;
DROP TABLE    ConfigurationESSourceAssoc;
DROP TABLE    ConfigurationESModuleAssoc;
DROP TABLE    ConfigurationParamSetAssoc;
DROP TABLE    ConfigurationContentAssoc;
DROP TABLE    ECStatementAssoc;
DROP TABLE    EventContentStatements;
DROP TABLE    ECStreamAssoc;
DROP TABLE    Paths;
DROP TABLE    Sequences;
DROP TABLE    Services;
DROP TABLE    ServiceTemplates;
DROP TABLE    EDSources;
DROP TABLE    EDSourceTemplates;
DROP TABLE    ESSources;
DROP TABLE    ESSourceTemplates;
DROP TABLE    ESModules;
DROP TABLE    ESModuleTemplates;
DROP TABLE    Modules;
DROP TABLE    ModuleTemplates;
DROP TABLE    ModuleTypes;
DROP TABLE    EventContents;
DROP TABLE    Streams;
DROP TABLE    PrimaryDatasets;
DROP TABLE    Configurations;
DROP TABLE    LockedConfigurations;
DROP TABLE    Directories;
DROP TABLE    SoftwareReleases;
DROP TABLE    SoftwarePackages;
DROP TABLE    SoftwareSubsystems;
DROP TABLE    Int32ParamValues;
DROP TABLE    VInt32ParamValues;
DROP TABLE    UInt32ParamValues;
DROP TABLE    VUInt32ParamValues;
DROP TABLE    Int64ParamValues;
DROP TABLE    VInt64ParamValues;
DROP TABLE    UInt64ParamValues;
DROP TABLE    VUInt64ParamValues;
DROP TABLE    BoolParamValues;
DROP TABLE    DoubleParamValues;
DROP TABLE    VDoubleParamValues;
DROP TABLE    StringParamValues;
DROP TABLE    VStringParamValues;
DROP TABLE    InputTagParamValues;
DROP TABLE    VInputTagParamValues;
DROP TABLE    EventIDParamValues;
DROP TABLE    VEventIDParamValues;
DROP TABLE    FileInPathParamValues;
DROP TABLE    SuperIdParameterAssoc;
DROP TABLE    SuperIdParamSetAssoc;
DROP TABLE    SuperIdVecParamSetAssoc;
DROP TABLE    ParameterSets;
DROP TABLE    VecParameterSets;
DROP TABLE    Parameters;
DROP TABLE    SuperIds;
DROP TABLE    ParameterTypes;


DROP SEQUENCE StatementId_Sequence;
DROP SEQUENCE EventContentId_Sequence;
DROP SEQUENCE ReleaseId_Sequence;
DROP SEQUENCE SubsysId_Sequence;
DROP SEQUENCE PackageId_Sequence;
DROP SEQUENCE DirId_Sequence;
--DROP SEQUENCE StreamId_Sequence;
DROP SEQUENCE DatasetId_Sequence;
DROP SEQUENCE ConfigId_Sequence;
DROP SEQUENCE SuperId_Sequence;
DROP SEQUENCE PathId_Sequence;
DROP SEQUENCE SequenceId_Sequence;
DROP SEQUENCE ParamId_Sequence;



