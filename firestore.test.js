const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);
const serverTimestamp = () => new Date();

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read universities, questions, or user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("universities").where("visibility", "==", "public").get());
  await assertFails(unauthDb.collection("verified_questions").where("visibility", "==", "public").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("test_records").get());
});

test("User profile: owner can create/update own profile; cross-user and ghost fields rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      preferredLanguage: "bn",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Ghost field shadow update fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredLanguage: "en",
      isAdmin: true,
      updatedAt: serverTimestamp(),
    })
  );

  // Valid update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredLanguage: "en",
      updatedAt: serverTimestamp(),
    })
  );

  // Cross-user read fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("User test records & items: owner can create and list; cross-user rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const recordRef = aliceDb.collection("users").doc(ALICE_UID).collection("test_records").doc("rec_1");
  const itemRef = recordRef.collection("items").doc("item_0");

  const batch = aliceDb.batch();
  batch.set(recordRef, {
    userId: ALICE_UID,
    mode: "MEDICAL",
    titleEn: "Medical Full Mock",
    titleBn: "মেডিকেল পূর্ণাঙ্গ মডেল টেস্ট",
    universityId: "medical_mbbs",
    unitId: "mbbs",
    sessionYear: "2024-25",
    sourceType: "AI_PRACTICE",
    totalQuestions: 100,
    attemptedCount: 95,
    correctCount: 80,
    wrongCount: 15,
    unansweredCount: 5,
    totalScore: 76.25,
    maxPossibleScore: 100,
    accuracyPercent: 84.21,
    durationSeconds: 3600,
    timeTakenSeconds: 2900,
    positiveMark: 1.0,
    negativeMark: 0.25,
    unansweredMark: 0.0,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });
  batch.set(itemRef, {
    questionIndex: 0,
    subject: "Biology",
    topic: "Cell Division",
    questionBn: "কোন ধাপে ক্রসিং ওভার ঘটে?",
    optionA: "লেপ্টোটিন",
    optionB: "জাইগোটিন",
    optionC: "প্যাকাইটিন",
    optionD: "ডিপ্লোটিন",
    correctOption: "C",
    selectedOption: "C",
    explanationBn: "মিয়োসিস-১ এর প্রোফেজ-১ এর প্যাকাইটিন উপ-পর্যায়ে ক্রসিং ওভার ঘটে।",
    isAiGenerated: true,
    sourceInfo: "AI Practice",
    createdAt: serverTimestamp(),
  });
  await assertSucceeds(batch.commit());

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("test_records").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("test_records").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("test_records").doc("rec_1").get());
});

test("Shared university catalogue & verified questions: query-aligned read allowed; only admin can write; spoofed email rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("admins").doc(ADMIN_UID).set({ role: "admin" });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spoofedDb = testEnv.authenticatedContext("spoof_1", {
    email: "mithunmandal8873@gmail.com",
    email_verified: false,
  }).firestore();
  const verifiedAdminEmailDb = testEnv.authenticatedContext("real_admin", {
    email: "mithunmandal8873@gmail.com",
    email_verified: true,
  }).firestore();

  const uniPayload = {
    nameEn: "University of Dhaka",
    nameBn: "ঢাকা বিশ্ববিদ্যালয়",
    abbreviation: "DU",
    category: "PUBLIC",
    sessionYear: "2024-25",
    sourceReference: "admission.eis.du.ac.bd",
    verificationDate: "2025-01-15",
    isPatternVerified: true,
    units: [],
    visibility: "public",
    updatedBy: ADMIN_UID,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };

  // Non-admin write fails
  await assertFails(
    aliceDb.collection("universities").doc("du").set({ ...uniPayload, updatedBy: ALICE_UID })
  );

  // Unverified spoofed admin email write fails
  await assertFails(
    spoofedDb.collection("universities").doc("du").set({ ...uniPayload, updatedBy: "spoof_1" })
  );

  // Admin write succeeds
  await assertSucceeds(adminDb.collection("universities").doc("du").set(uniPayload));

  // Verified admin email write succeeds
  await assertSucceeds(
    verifiedAdminEmailDb.collection("universities").doc("buet").set({
      ...uniPayload,
      nameEn: "BUET",
      nameBn: "বাংলাদেশ প্রকৌশল বিশ্ববিদ্যালয়",
      abbreviation: "BUET",
      category: "ENGINEERING",
      updatedBy: "real_admin",
    })
  );

  // Non-admin query WITH visibility == 'public' succeeds
  await assertSucceeds(
    aliceDb.collection("universities").where("visibility", "==", "public").get()
  );

  // Non-admin unfiltered list FAILS (Query Enforcer)
  await assertFails(aliceDb.collection("universities").get());
});
