# /users/sharper/2020/egamma/smallMenu/V114 (CMSSW_11_1_0_pre5)

import FWCore.ParameterSet.Config as cms

process = cms.Process( "HLT" )

process.HLTConfigVersion = cms.PSet(
  tableName = cms.string('/users/sharper/2020/egamma/smallMenu/V114')
)

process.source = cms.Source( "PoolSource",
    fileNames = cms.untracked.vstring( 'test.root' )
)

process.BeamSpotOnlineProducer = cms.EDProducer( "BeamSpotOnlineProducer",
    maxZ = cms.double( 10000.0 ),
    src = cms.InputTag( "scalersRawToDigi" ),
    gtEvmLabel = cms.InputTag( "gtEvmDigis" ),
    changeToCMSCoordinates = cms.bool( False ),
    setSigmaZ = cms.double( -1.0 ),
    maxRadius = cms.double( 2.0 )
)
process.BasicToPFJet = cms.EDProducer( "BasicToPFJet",
    src = cms.InputTag( "" )
)
process.BVertexFilter = cms.EDFilter( "BVertexFilter",
    primaryVertices = cms.InputTag( "offlinePrimaryVertices" ),
    minVertices = cms.int32( 2222 ),
    useVertexKinematicAsJetAxis = cms.bool( True ),
    vertexFilter = cms.PSet( 
      distSig3dMax = cms.double( 99999.9 ),
      fracPV = cms.double( 0.65 ),
      distVal2dMax = cms.double( 2.5 ),
      useTrackWeights = cms.bool( True ),
      maxDeltaRToJetAxis = cms.double( 0.1 ),
      v0Filter = cms.PSet(  k0sMassWindow = cms.double( 0.05 ) ),
      distSig2dMin = cms.double( 3.0 ),
      multiplicityMin = cms.uint32( 2 ),
      distVal2dMin = cms.double( 0.01 ),
      distSig2dMax = cms.double( 99999.9 ),
      distVal3dMax = cms.double( 99999.9 ),
      minimumTrackWeight = cms.double( 0.5 ),
      distVal3dMin = cms.double( -99999.9 ),
      massMax = cms.double( 6.5 ),
      distSig3dMin = cms.double( -99999.9 )
    ),
    secondaryVertices = cms.InputTag( "secondaryVertices" )
)
process.CSCViewDigi = cms.EDAnalyzer( "CSCViewDigi",
    StripDigiDump = cms.untracked.bool( True ),
    WiresDigiDump = cms.untracked.bool( True ),
    DCCStatus = cms.untracked.bool( True ),
    StatusDigiDump = cms.untracked.bool( False ),
    rpcDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCRPCDigi' ),
    ClctDigiDump = cms.untracked.bool( True ),
    alctDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCALCTDigi' ),
    statusDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCDCCFormatStatusDigi' ),
    DDUStatus = cms.untracked.bool( True ),
    StatusCFEBDump = cms.untracked.bool( True ),
    comparatorDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCComparatorDigi' ),
    statusCFEBTag = cms.InputTag( 'muonCSCDigis','MuonCSCCFEBStatusDigi' ),
    DDUstatusDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCDDUStatusDigi' ),
    wireDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCWireDigi' ),
    AlctDigiDump = cms.untracked.bool( True ),
    clctDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCCLCTDigi' ),
    RpcDigiDump = cms.untracked.bool( True ),
    stripDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCStripDigi' ),
    DCCstatusDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCDCCStatusDigi' ),
    CorrClctDigiDump = cms.untracked.bool( True ),
    corrclctDigiTag = cms.InputTag( 'muonCSCDigis','MuonCSCCorrelatedLCTDigi' ),
    ComparatorDigiDump = cms.untracked.bool( True )
)

process.TestSequence = cms.Sequence( process.CSCViewDigi )

process.b = cms.Task( process.BeamSpotOnlineProducer )
process.a = cms.Task( process.b + process.BasicToPFJet )
process.TestTask1 = cms.Task( process.BVertexFilter )

process.HLT_DoubleEle25_CaloIdL_MW_v4 = cms.Path( process.a + process.TestTask1 + process.TestSequence )
process.output = cms.EndPath( process.out )


process.HLTSchedule = cms.Schedule( *(process.HLT_DoubleEle25_CaloIdL_MW_v4, process.output ))
