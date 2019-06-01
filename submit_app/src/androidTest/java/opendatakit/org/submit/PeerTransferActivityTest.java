//package opendatakit.org.submit;
//
//import android.content.ContentValues;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.filters.LargeTest;
//import android.support.test.runner.AndroidJUnit4;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.opendatakit.aggregate.odktables.rest.entity.Column;
//import org.opendatakit.database.data.*;
//import org.opendatakit.database.service.DbHandle;
//import org.opendatakit.database.service.UserDbInterfaceImpl;
//import org.opendatakit.exception.ActionNotAuthorizedException;
//import org.opendatakit.exception.ServicesAvailabilityException;
//import org.opendatakit.properties.CommonToolProperties;
//import org.opendatakit.properties.PropertiesSingleton;
//import org.opendatakit.provider.DataTableColumns;
//import org.opendatakit.submit.activities.PeerTransferActivity;
//import org.opendatakit.submit.consts.CommonInfo;
//import org.opendatakit.submit.consts.SubmitColumns;
//import org.opendatakit.submit.consts.SubmitSyncStates;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.junit.Assert.*;
//
//@RunWith(AndroidJUnit4.class)
//@LargeTest
//public class PeerTransferActivityTest extends OdkDatabaseTestAbstractBase {
//
//    private static final String TAG = "PeerTransferActivityTest";
//    private UserDbInterfaceImpl dbInterface;
//    private DbHandle db;
//    private PropertiesSingleton props;
//    private OrderedColumns cols;
//
//    private static final String SYNCED_A_TABLE_ID = "syncedA";
//    private static final String SYNCED_B_TABLE_ID = "syncedB";
//    private static final String DIFF_NEW_ROWS_B_TABLE_ID = "diffNewRowsB";
//    private static final String SYNCED_NEW_E_TAGS_B_TABLE_ID = "syncedNewETagsB";
//    private static final String ALL_DIFF_A_TABLE_ID = "allDiffA";
//    private static final String ALL_DIFF_B_TABLE_ID = "allDiffB";
//
//    private static final String DEVICE_ID_A = "A";
//    private static final String DEVICE_ID_B = "B";
//
//    private static final String TRANSFER_ID_MOCK = "mocktransfer";
//
//    @Override
//    protected void setUpBefore() {
//        try {
//            dbInterface = bindToDbService();
//            db = dbInterface.openDatabase(APPNAME);
//
//            props = CommonToolProperties.get(InstrumentationRegistry.getTargetContext(), APPNAME);
//            props.clearSettings();
//
//            genTables();
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//
//    @Override
//    protected void tearDownBefore() {
//        try {
//            props.clearSettings();
//            if (db != null) {
//                List<String> tableIds = dbInterface.getAllTableIds(APPNAME, db);
//                for (String id : tableIds) {
//                    dbInterface.deleteTableAndAllData(APPNAME, db, id);
//                    dbInterface.deleteTableMetadata(APPNAME, db, id, null, null, null);
//                }
//                dbInterface.closeDatabase(APPNAME, db);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//
//    /**
//     * Selects all rows (and all columns) from the table tableId
//     *
//     * @param tableId the Id for the table
//     * @return a UserTable representation of the table tableId
//     * @throws ServicesAvailabilityException
//     */
//    private UserTable selectAllFrom(String tableId) throws ServicesAvailabilityException {
//        String select = "SELECT * FROM " + tableId;
//        return dbInterface.arbitrarySqlQuery(APPNAME, db, tableId, cols, select,
//                null, null, null);
//    }
//
//    /**
//     * tests sync between SYNCED_A_TABLE_ID and SYNCED_B_TABLE_ID
//     *
//     * tests when both tables are both completely p_synced
//     */
//    @Test
//    public void testSyncedRows() {
//        //InstrumentationRegistry.getContext().
//        CommonInfo info = new CommonInfo(dbInterface, db, APPNAME);
//        try {
//            PeerTransferActivity.sync(info, SYNCED_A_TABLE_ID, SYNCED_B_TABLE_ID);
//
//            Map<String, List<TypedRow>> A = genIndexMap(selectAllFrom(SYNCED_A_TABLE_ID));
//            Map<String, List<TypedRow>> B = genIndexMap(selectAllFrom(SYNCED_B_TABLE_ID));
//
//            assertSynced(A, B);
//        } catch (ServicesAvailabilityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * tests sync between SYNCED_A_TABLE_ID and DIFF_NEW_ROWS_B_TABLE_ID
//     *
//     * tests when one table has additional p_synced, p_modified, p_conflict, and p_divergent rows
//     */
//    @Test
//    public void testAdditionalRows() {
//        CommonInfo info = new CommonInfo(dbInterface, db, APPNAME);
//        try {
//            PeerTransferActivity.sync(info, SYNCED_A_TABLE_ID, DIFF_NEW_ROWS_B_TABLE_ID);
//
//            Map<String, List<TypedRow>> A = genIndexMap(selectAllFrom(SYNCED_A_TABLE_ID));
//            Map<String, List<TypedRow>> B = genIndexMap(selectAllFrom(DIFF_NEW_ROWS_B_TABLE_ID));
//
//            // same rows still synced
//            assertSynced(A, B);
//            // extra row in B - p_synced
//            assertAdditionalSingle(A.get("4"), B.get("4"), SubmitSyncStates.P_SYNCED);
//            // extra row in B - p_modified
//            assertAdditionalSingle(A.get("7"), B.get("7"), SubmitSyncStates.P_MODIFIED);
//            // extra rows in B - p_conflict
//            assertAdditionalDuplicates(A.get("5"), B.get("5"), SubmitSyncStates.P_CONFLICT);
//            // extra row in B - p_divergent
//            assertAdditionalDuplicates(A.get("6"), B.get("6"), SubmitSyncStates.P_DIVERGENT);
//
//        } catch (ServicesAvailabilityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * tests sync between SYNCED_A_TABLE_ID and SYNCED_NEW_E_TAGS_B_TABLE_ID
//     *
//     * tests sync for p_synced rows with different eTags
//     */
//    @Test
//    public void testSyncedToDivergent() {
//        CommonInfo info = new CommonInfo(dbInterface, db, APPNAME);
//        try {
//            PeerTransferActivity.sync(info, SYNCED_A_TABLE_ID, SYNCED_NEW_E_TAGS_B_TABLE_ID);
//
//            Map<String, List<TypedRow>> A = genIndexMap(selectAllFrom(SYNCED_A_TABLE_ID));
//            Map<String, List<TypedRow>> B = genIndexMap(selectAllFrom(SYNCED_NEW_E_TAGS_B_TABLE_ID));
//
//            for (int i = 0; i < 4; i++) {
//                assertCommonDuplicates(A.get(Integer.toString(i)), B.get(Integer.toString(i)),
//                        SubmitSyncStates.P_DIVERGENT);
//            }
//        } catch (ServicesAvailabilityException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * tests sync between ALL_DIFF_A_TABLE_ID and ALL_DIFF_B_TABLE_ID
//     *
//     * tests when two rows are in p_conflict or p_divergent on both devices, and
//     * nothing needs to be updated
//     */
//    @Test
//    public void testConflictingUnchangedRows() {
//        CommonInfo info = new CommonInfo(dbInterface, db, APPNAME);
//        try {
//            PeerTransferActivity.sync(info, ALL_DIFF_A_TABLE_ID, ALL_DIFF_B_TABLE_ID);
//
//            Map<String, List<TypedRow>> A = genIndexMap(selectAllFrom(ALL_DIFF_A_TABLE_ID));
//            Map<String, List<TypedRow>> B = genIndexMap(selectAllFrom(ALL_DIFF_B_TABLE_ID));
//
//            assertConflictingNoChange(A.get("3"), B.get("3"), SubmitSyncStates.P_DIVERGENT);
//            assertConflictingNoChange(A.get("11"), B.get("11"), SubmitSyncStates.P_CONFLICT);
//
//        } catch (ServicesAvailabilityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * tests sync between ALL_DIFF_A_TABLE_ID and ALL_DIFF_B_TABLE_ID
//     *
//     * tests sync between all combinations of states that end up in a p_conflict state
//     */
//    @Test
//    public void testConflictGeneration() {
//        CommonInfo info = new CommonInfo(dbInterface, db, APPNAME);
//        try {
//            PeerTransferActivity.sync(info, ALL_DIFF_A_TABLE_ID, ALL_DIFF_B_TABLE_ID);
//
//            Map<String, List<TypedRow>> A = genIndexMap(selectAllFrom(ALL_DIFF_A_TABLE_ID));
//            Map<String, List<TypedRow>> B = genIndexMap(selectAllFrom(ALL_DIFF_B_TABLE_ID));
//
//            assertCommonDuplicates(A.get("1"), B.get("1"), SubmitSyncStates.P_CONFLICT);
//            assertCommonDuplicates(A.get("5"), B.get("5"), SubmitSyncStates.P_CONFLICT);
//            assertCommonDuplicates(A.get("6"), B.get("6"), SubmitSyncStates.P_CONFLICT);
//            assertCommonDuplicates(A.get("7"), B.get("7"), SubmitSyncStates.P_CONFLICT);
//            assertCommonDuplicates(A.get("8"), B.get("8"), SubmitSyncStates.P_CONFLICT);
//            assertCommonDuplicates(A.get("9"), B.get("9"), SubmitSyncStates.P_CONFLICT);
//
//        } catch (ServicesAvailabilityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * tests sync between ALL_DIFF_A_TABLE_ID and ALL_DIFF_B_TABLE_ID
//     *
//     * tests sync between all combinations of states that end up in a p_divergent state
//     */
//    @Test
//    public void testDivergentGeneration() {
//        CommonInfo info = new CommonInfo(dbInterface, db, APPNAME);
//        try {
//            PeerTransferActivity.sync(info, ALL_DIFF_A_TABLE_ID, ALL_DIFF_B_TABLE_ID);
//
//            Map<String, List<TypedRow>> A = genIndexMap(selectAllFrom(ALL_DIFF_A_TABLE_ID));
//            Map<String, List<TypedRow>> B = genIndexMap(selectAllFrom(ALL_DIFF_B_TABLE_ID));
//
//            assertCommonDuplicates(A.get("4"), B.get("4"), SubmitSyncStates.P_DIVERGENT);
//            assertCommonDuplicates(A.get("10"), B.get("10"), SubmitSyncStates.P_DIVERGENT);
//
//        } catch (ServicesAvailabilityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // ############################################################################################
//    // # helper methods below
//    // ############################################################################################
//
//    /**
//     * Maps all rows in UserTable table to a map of P_ID's -> Rows
//     *
//     * @param table the UserTable to map
//     * @return a Map of P_ID's -> Rows
//     */
//    private static Map<String, List<TypedRow>> genIndexMap(UserTable table) {
//        Map<String, List<TypedRow>> result = new HashMap<>();
//        for (int i = 0; i < table.getNumberOfRows(); i++) {
//            TypedRow row = table.getRowAtIndex(i);
//            String pid = row.getStringValueByKey(SubmitColumns.P_ID);
//            if (!result.containsKey(pid)) {
//                result.put(pid, new ArrayList<TypedRow> ());
//            }
//            result.get(pid).add(row);
//        }
//        return result;
//    }
//
//    /**
//     * asserts nothing has changed during sync between rows in p_conflict or p_divergent
//     * when both versions of the row already exist on both devices
//     *
//     * @param rowsA list of (two) rows on device A
//     * @param rowsB list of (two) rows on device B
//     * @param state either p_conflict or p_divergent
//     */
//    private static void assertConflictingNoChange(List<TypedRow>  rowsA, List<TypedRow>  rowsB, String state) {
//        assertEquals(2, rowsA.size());
//        assertEquals(2, rowsB.size());
//
//        TypedRow rowA3_0 = rowsA.get(0);
//        TypedRow rowB3_0 = rowsB.get(0);
//        TypedRow rowA3_1 = rowsA.get(1);
//        TypedRow rowB3_1 = rowsB.get(1);
//
//        assertTrue(rowA3_0.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowB3_0.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowA3_1.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowB3_1.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        // we're saying everything originated from A even though that doesn't make sense
//        // (all we need to do is check nothing changed)
//        assertTrue(rowA3_0.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//        assertTrue(rowB3_0.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//        assertTrue(rowA3_1.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//        assertTrue(rowB3_1.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//        // they should all have their mock transfer id
//        assertTrue(rowA3_0.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//        assertTrue(rowB3_0.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//        assertTrue(rowA3_1.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//        assertTrue(rowB3_1.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//    }
//
//    /**
//     * asserts the first four rows (the ones in SYNCED_A_TABLE_ID) stay p_synced
//     *
//     * @param A Map representation of table A
//     * @param B Map representation of table B
//     */
//    private static void assertSynced(Map<String, List<TypedRow>> A, Map<String, List<TypedRow>> B) {
//        for (int i = 0; i < 4; i++) {
//            TypedRow rowA = A.get(Integer.toString(i)).get(0);
//            TypedRow rowB = B.get(Integer.toString(i)).get(0);
//            // everything should be synced, and belong to their respective origin table
//            // (lets say all of these rows came from A originally)
//            assertTrue(rowA.getStringValueByKey(SubmitColumns.P_STATE).equals(SubmitSyncStates.P_SYNCED));
//            assertTrue(rowA.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//            assertTrue(rowB.getStringValueByKey(SubmitColumns.P_STATE).equals(SubmitSyncStates.P_SYNCED));
//            assertTrue(rowB.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//            // these all happened during the mock transfer
//            assertTrue(rowA.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//            assertTrue(rowB.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//        }
//    }
//
//    /**
//     * asserts correct sync behavior for additional modified and synced row
//     *
//     * @param rowsA list of (one) row from table A
//     * @param rowsB list of (one) row from table B
//     * @param state either p_conflict or p_divergent
//     */
//    private static void assertAdditionalSingle(List<TypedRow>  rowsA, List<TypedRow>  rowsB, String state) {
//        TypedRow rowA = rowsA.get(0);
//        TypedRow rowB = rowsB.get(0);
//        assertTrue(rowA.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowB.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowA.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        assertTrue(rowB.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        // transfer id of A should be updated, while B should stay the same
//        assertTransferIdUpdated(rowA, rowB);
//    }
//
//    /**
//     * asserts correct sync behavior for additional conflict and divergent rows
//     *
//     * @param rowsA list of (two) rows from table A
//     * @param rowsB list of (two) rows from table B
//     * @param state either p_conflict or p_divergent
//     */
//    private static void assertAdditionalDuplicates(List<TypedRow>  rowsA, List<TypedRow>  rowsB, String state) {
//        assertTrue(rowsA.size() == 2);
//        assertTrue(rowsB.size() == 2);
//        TypedRow rowA_0 = rowsA.get(0);
//        TypedRow rowA_1 = rowsA.get(1);
//        assertTrue(rowA_0.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowA_1.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowA_0.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        assertTrue(rowA_1.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        TypedRow rowB_0 = rowsB.get(0);
//        TypedRow rowB_1 = rowsB.get(1);
//        assertTrue(rowB_0.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowB_1.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowB_0.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        assertTrue(rowB_1.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        // transfer id of A should be updated, while B should stay the same
//        assertTransferIdUpdated(rowA_0, rowB_0);
//        assertTransferIdUpdated(rowA_1, rowB_1);
//    }
//
//    /**
//     * asserts correct sync behavior for rows that need to be put in p_conflict or p_divergent
//     * on both devices
//     *
//     * @param rowsA list of (two) rows from table A
//     * @param rowsB list of (two) rows from table B
//     * @param state either p_conflict or p_divergent
//     */
//    private static void assertCommonDuplicates(List<TypedRow>  rowsA, List<TypedRow>  rowsB, String state) {
//        assertTrue(rowsA.size() == 2);
//        assertTrue(rowsB.size() == 2);
//        TypedRow rowA_0 = rowsA.get(0);
//        TypedRow rowA_1 = rowsA.get(1);
//        assertTrue(rowA_0.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowA_1.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        // rowA_0 was originally from device A, rowA_1 came from device B during the transfer
//        assertTrue(rowA_0.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//        assertTrue(rowA_1.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        TypedRow rowB_0 = rowsB.get(0);
//        TypedRow rowB_1 = rowsB.get(1);
//        assertTrue(rowB_0.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        assertTrue(rowB_1.getStringValueByKey(SubmitColumns.P_STATE).equals(state));
//        // rowB_0 was originally from device B, rowB_1 came from device A during the transfer
//        assertTrue(rowB_0.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_B));
//        assertTrue(rowB_1.getStringValueByKey(SubmitColumns.DEVICE_ID).equals(DEVICE_ID_A));
//        // rowA_0 originated from device A, rowA_1 came in from device B
//        assertTransferIdUpdated(rowA_1, rowA_0);
//        // rowB_0 originated from device B, rowB_1 came in from device A
//        assertTransferIdUpdated(rowB_1, rowB_0);
//    }
//
//    /**
//     * transfer id of A should be updated, while B should stay the same
//     */
//    private static void assertTransferIdUpdated(TypedRow rowA, TypedRow rowB) {
//        assertFalse(rowA.getStringValueByKey(SubmitColumns.TRANSFER_ID)
//                .equals(rowB.getStringValueByKey(SubmitColumns.TRANSFER_ID)));
//        assertTrue(rowB.getStringValueByKey(SubmitColumns.TRANSFER_ID).equals(TRANSFER_ID_MOCK));
//    }
//
//    /**
//     * Generates all tables
//     *
//     * @throws ServicesAvailabilityException
//     * @throws ActionNotAuthorizedException
//     */
//    private void genTables() throws ServicesAvailabilityException, ActionNotAuthorizedException {
//
//        // GENERATE COLUMNS
//        List<Column> columns = new ArrayList<>();
//        columns.add(new Column(SubmitColumns.P_STATE, SubmitColumns.P_STATE, "string", null));
//        columns.add(new Column(SubmitColumns.DEVICE_ID, SubmitColumns.DEVICE_ID, "string", null));
//        columns.add(new Column(SubmitColumns.TRANSFER_ID, SubmitColumns.TRANSFER_ID, "string", null));
//        columns.add(new Column(SubmitColumns.P_ID, SubmitColumns.P_ID, "string", null));
//        ColumnList colList = new ColumnList(columns);
//
//        // #########################################################################################
//        // !!!NEW MOCK TABLES!!! local and peer "synced" tables are identical and already synced
//        // #########################################################################################
//
//        // the ordered columns are the same for all tables
//        cols = dbInterface.createOrOpenTableWithColumns(APPNAME, db, SYNCED_A_TABLE_ID, colList);
//        dbInterface.createOrOpenTableWithColumns(APPNAME, db, SYNCED_B_TABLE_ID, colList);
//
//        for (int i = 0; i < 4; i++) {
//            String eTag = Integer.toString(i);
//            insertRowWithState(SubmitSyncStates.P_SYNCED, i, SYNCED_A_TABLE_ID, DEVICE_ID_A, eTag);
//            insertRowWithState(SubmitSyncStates.P_SYNCED, i, SYNCED_B_TABLE_ID, DEVICE_ID_A, eTag);
//        }
//
//        // #########################################################################################
//        // !!!NEW MOCK TABLE!!! with everything localSame has but synced rows have a different etag
//        // #########################################################################################
//
//        dbInterface.createOrOpenTableWithColumns(APPNAME, db, SYNCED_NEW_E_TAGS_B_TABLE_ID, colList);
//
//        for (int i = 0; i < 4; i++) {
//            insertRowWithState(SubmitSyncStates.P_SYNCED, i, SYNCED_NEW_E_TAGS_B_TABLE_ID,
//                    DEVICE_ID_B, Integer.toString(i) + ".5");
//        }
//
//        // #########################################################################################
//        // !!!NEW MOCK TABLE!!! with everything localSame has and more new rows
//        // #########################################################################################
//
//        dbInterface.createOrOpenTableWithColumns(APPNAME, db, DIFF_NEW_ROWS_B_TABLE_ID, colList);
//
//        for (int i = 0; i < 4; i++) {
//            String eTag = Integer.toString(i);
//            insertRowWithState(SubmitSyncStates.P_SYNCED, i, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_A,
//                    eTag);
//        }
//        // extra sync row
//        insertRowWithState(SubmitSyncStates.P_SYNCED, 4, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_B,
//                "4");
//        // extra conflict rows
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 5, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_B,
//                "5");
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 5, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_B,
//                "5.5");
//        // extra divergent rows
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 6, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_B,
//                "6");
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 6, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_B,
//                "6.5");
//        // extra modified row
//        insertRowWithState(SubmitSyncStates.P_MODIFIED, 7, DIFF_NEW_ROWS_B_TABLE_ID, DEVICE_ID_B,
//                "7");
//
//        // #########################################################################################
//        // !!!NEW MOCK TABLES!!! with all possible differences in states
//        // #########################################################################################
//
//        dbInterface.createOrOpenTableWithColumns(APPNAME, db, ALL_DIFF_A_TABLE_ID, colList);
//        dbInterface.createOrOpenTableWithColumns(APPNAME, db, ALL_DIFF_B_TABLE_ID, colList);
//
//        // this should be good
//        insertRowWithState(SubmitSyncStates.P_SYNCED, 2, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "2");
//        insertRowWithState(SubmitSyncStates.P_SYNCED, 2, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "2");
//
//        // this should stay divergent (not change during sync)
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 3, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "3");
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 3, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "3.5");
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 3, ALL_DIFF_B_TABLE_ID, DEVICE_ID_A,
//                "3");
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 3, ALL_DIFF_B_TABLE_ID, DEVICE_ID_A,
//                "3.5");
//
//        // these should go to divergent
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 4, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "4");
//        insertRowWithState(SubmitSyncStates.P_SYNCED, 4, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "4.5");
//
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 10, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "10");
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 10, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "10.5");
//
//        // this should stay conflict (not change during sync)
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 11, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "11");
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 11, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "11.5");
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 11, ALL_DIFF_B_TABLE_ID, DEVICE_ID_A,
//                "11");
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 11, ALL_DIFF_B_TABLE_ID, DEVICE_ID_A,
//                "11.5");
//
//        // all of these go to conflict
//        insertRowWithState(SubmitSyncStates.P_MODIFIED, 5, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "5.5");
//        insertRowWithState(SubmitSyncStates.P_SYNCED, 5, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "5");
//
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 6, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "6");
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 6, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "6.5");
//
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 7, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "7");
//        insertRowWithState(SubmitSyncStates.P_MODIFIED, 7, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "7.5");
//
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 8, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "8");
//        insertRowWithState(SubmitSyncStates.P_SYNCED, 8, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "8.5");
//
//        insertRowWithState(SubmitSyncStates.P_DIVERGENT, 9, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "9");
//        insertRowWithState(SubmitSyncStates.P_CONFLICT, 9, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "9.5");
//
//        insertRowWithState(SubmitSyncStates.P_MODIFIED, 1, ALL_DIFF_A_TABLE_ID, DEVICE_ID_A,
//                "1");
//        insertRowWithState(SubmitSyncStates.P_MODIFIED, 1, ALL_DIFF_B_TABLE_ID, DEVICE_ID_B,
//                "1.5");
//    }
//
//    /**
//     * Inserts a row with specified id and state into tableId with "name" column populated with id
//     * (helper method for genTables
//     *
//     * @param pState the peer state of the row
//     * @param pId the peer id for the row (later will be the real id)
//     * @param tableId id for table
//     * @param deviceId id for device
//     * @throws ServicesAvailabilityException
//     * @throws ActionNotAuthorizedException
//     */
//    private void insertRowWithState(String pState, int pId, String tableId, String deviceId,
//                                    String eTag)
//            throws ServicesAvailabilityException, ActionNotAuthorizedException {
//
//        // todo: quick fix to handle conflicts - unique _id for everything, same _p_id for same rows
//        // todo: future design - same id for same rows, random _p_id as primary key
//        String id = UUID.randomUUID().toString();
//
//        //todo: we need a new table schema, but for now our metadata values will live as user cols
//        ContentValues content = new ContentValues();
//        content.put(SubmitColumns.P_STATE, pState);
//        content.put(SubmitColumns.P_ID, Integer.toString(pId));
//        content.put(SubmitColumns.DEVICE_ID, deviceId);
//        content.put(DataTableColumns.ROW_ETAG, eTag);
//        // initialize everything with the "mock" transfer id
//        content.put(SubmitColumns.TRANSFER_ID, TRANSFER_ID_MOCK);
//
//        dbInterface.insertRowWithId(APPNAME, db, tableId, cols, content, id);
//    }
//
//}