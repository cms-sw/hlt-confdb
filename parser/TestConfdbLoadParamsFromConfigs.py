from ConfdbLoadParamsFromConfigs141 import ConfdbLoadParamsfromConfigs
import FWCore.ParameterSet.Config as cms

class DummyLoader(object):
    def ConfdbInsertPackageSubsystem(self,cursor,subsystem,package):
        return 1
    def ConfdbExitGracefully(self):
        pass

class DummyCursor(object):
    def __init__(self, verbose = False):
        self.lastExec_ = None
        self.inserts_ = []
        self.verbose_ = verbose
    def execute(self, command, PLOB=None):
        self.lastExec_ = command
        if 'INSERT INTO' in command:
            self.inserts_.append(command)
        if not self.verbose_:
            return
        if PLOB is not None:
            print(command,"PLOB=",PLOB)
        else:
            print(command)
    def fetchone(self):
        if 'SELECT u_softreleases.Id' in self.lastExec_:
            return list()
        if "a.name = 'MessageLogger'" in self.lastExec_:
            return [-1]
        return list((1,))
    def fetchall(self):
        if 'SELECT name,id  FROM u_paramtypes' in self.lastExec_:
            return [('int32',0),('vint32',1),
                    ('uint32',2), ('vuint32',3),
                    ('int64', 4), ('vint64',5),
                    ('uint64',6), ('vuint64',7),
                    ('bool',8),
                    ('double',9), ('vdouble',10),
                    ('string', 11), ('vstring', 12),
                    ('InputTag', 13), ('VInputTag', 14),
                    ('ESInputTag',15), ('VESInputTag', 16),
                    ('EventID', 17), ('VEventID', 17),
                    ('FileInPath',18)]
        return list()
    def __iter__(self):
        class _Iterator(object):
            def __next__(self):
                raise StopIteration
        return _Iterator()

if __name__ == "__main__":
    def wasInserted(name, inserts):
        for i in inserts:
            if name in i:
                return True
        return False
    
    def setupParser(dbloader, dbcursor, verbosity=0):
        db = ConfdbLoadParamsfromConfigs(
            clirel = '',
            clibasepath = '',
            clibasereleasepath = '',
            cliwhitelist = '',
            cliblacklist = '',
            cliusingwhitelist = False,
            cliusingblacklist = False,
            cliverbose = verbosity,
            clidbuser = '',
            clidbpwd = '',
            clihost = '',
            clinoload = False,
            cliaddtorelease = False,
            clicomparetorelease = '',
            clipreferfile = '',
            cliarch = '',
            dbtester = (dbloader, dbcursor)
        )
        db.SetupTables()
        return db
    import unittest
    class testParser(unittest.TestCase):
        def testVPSet(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.VPSet(
                                      cms.PSet(foo = cms.PSet(bar = cms.int32(1)))))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("bar", cursor.inserts_))
            self.assertTrue(wasInserted("foo", cursor.inserts_))
            self.assertTrue(wasInserted("a_", cursor.inserts_))
        def testVPSetWithLabelVPSet(self):
            cursor = DummyCursor(verbose=True)
            parser = setupParser(DummyLoader(), cursor, verbosity=3)
            prod = cms.EDProducer("D",
                                  VPSet = cms.VPSet(
                                      cms.PSet(foo = cms.PSet(bar = cms.int32(1)))))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("VPSet", cursor.inserts_))
            self.assertTrue(wasInserted("bar", cursor.inserts_))
            #This fails because the parser drops the label because of how the parser
            # handles seeing the string 'VPSet['
            self.assertTrue(wasInserted("foo", cursor.inserts_))
        def testPSetWithLabelVPSet(self):
            cursor = DummyCursor(verbose=False)
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  VPSet = cms.PSet(
                                    foo = cms.PSet(bar = cms.int32(1))))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("VPSet", cursor.inserts_))
            self.assertTrue(wasInserted("bar", cursor.inserts_))
            self.assertTrue(wasInserted("foo", cursor.inserts_))
        def testPSetWithLabelVPSetHoldingAVPSet(self):
            cursor = DummyCursor(verbose=False)
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  VPSet = cms.PSet(
                                      a_ = cms.VPSet( cms.PSet(
                                    foo = cms.PSet(bar = cms.int32(1))))))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("VPSet", cursor.inserts_))
            self.assertTrue(wasInserted("bar", cursor.inserts_))
            self.assertTrue(wasInserted("foo", cursor.inserts_))
        def testvstringWithLabelVPSet(self):
            cursor = DummyCursor(verbose=False)
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  VPSet = cms.vstring("foo", "bar"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("VPSet", cursor.inserts_))
        def testPSetRecursion(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.PSet(foo = cms.PSet(bar = cms.int32(1))))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("bar", cursor.inserts_))
            self.assertTrue(wasInserted("foo", cursor.inserts_))
            self.assertTrue(wasInserted("a_", cursor.inserts_))
        def testOptional(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.optional.int32,
                                  b_ = cms.string("value"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("b_", cursor.inserts_))
            self.assertFalse(wasInserted("a_", cursor.inserts_))
        def testRequired(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.required.int32,
                                  b_ = cms.string("value"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("b_", cursor.inserts_))
            self.assertFalse(wasInserted("a_", cursor.inserts_))
        def testRequiredPSetTemplate(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.required.PSetTemplate(d_ = cms.required.int32, e_ = cms.double(3.2)),
                                  b_ = cms.string("value"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("b_", cursor.inserts_))
            self.assertFalse(wasInserted("a_", cursor.inserts_))
        def testOptionalPSetTemplate(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.optional.PSetTemplate(d_ = cms.required.int32, e_ = cms.double(3.2)),
                                  b_ = cms.string("value"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("b_", cursor.inserts_))
            self.assertFalse(wasInserted("a_", cursor.inserts_))
        def testRequiredVPSetTemplate(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.required.VPSetTemplate(cms.PSetTemplate(d_ = cms.required.int32, e_ = cms.double(3.2))),
                                  b_ = cms.string("value"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("b_", cursor.inserts_))
            self.assertFalse(wasInserted("a_", cursor.inserts_))
        def testOptionalVPSetTemplate(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.optional.VPSetTemplate(cms.PSetTemplate(d_ = cms.required.int32, e_ = cms.double(3.2))),
                                  b_ = cms.string("value"))
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("b_", cursor.inserts_))
            self.assertFalse(wasInserted("a_", cursor.inserts_))
        def testVPSetWithTemplate(self):
            cursor = DummyCursor()
            parser = setupParser(DummyLoader(), cursor)
            prod = cms.EDProducer("D",
                                  a_ = cms.VPSet(
                                      cms.PSet(foo = cms.int32(1)),
                                      cms.PSet(foo = cms.int32(2)),
                                      template = cms.PSetTemplate(foo = cms.required.int32,
                                                                  bar = cms.optional.string))
                                  )
            parser.componenttable = "u_moduletemplates"
            parser.FindParamsFromPython("Sub","Pkg", {"foo":prod}, "EDProducer", True)
            self.assertTrue(wasInserted("foo", cursor.inserts_))
            self.assertTrue(wasInserted("a_", cursor.inserts_))
    unittest.main()

        
